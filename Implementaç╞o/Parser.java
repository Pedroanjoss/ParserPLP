import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

enum EstadoParser {
    Q0,  // Estado inicial e de aceitação
    Q1,  // Após ler um identificador
    Q2,  // Após ler '='
    Q3,  // Após ler um número ou expressão aritmética
    Q4,  // Após ler 'print'
    Q5,  // Após ler um identificador para 'print'
    Q_IF, // Após ler 'if'
    Q_WHILE, // Após ler 'while'
    Q_BLOCK_CONDITION,  // Após abrir parêntese de condição (if/while)
    Q_COND_EXPR,  // Após ler a expressão condicional
    Q_BLOCK_START,  // Após fechar parêntese e esperar bloco de código
    Q_BLOCK // Após abrir um bloco de código '{'
}

public class Parser {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Parser <nome-do-arquivo>");
            return;
        }

        String nomeArquivo = args[0];
        String entrada = "";

        Parser p = new Parser();

        try (BufferedReader leitor = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null)
                entrada += linha;
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return;
        }

        boolean resultado = p.processar(entrada);
        System.out.println("Entrada " + (resultado ? "valida" : "invalida"));
    }

    // Função para remover múltiplos espaços
    private String formatarEntrada(String entrada) {
        // Remover múltiplos espaços e substituir por um único espaço
        return entrada.replaceAll("\\s+", " ").trim();
    }

    private List<String> tokenizar(String entrada) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < entrada.length()) {
            char charAtual = entrada.charAt(i);

            if (Character.isLetter(charAtual)) {  // Identificadores ou palavras-chave
                StringBuilder palavra = new StringBuilder();
                while (i < entrada.length() && (Character.isLetter(entrada.charAt(i)) || Character.isDigit(entrada.charAt(i)))) {
                    palavra.append(entrada.charAt(i));
                    i++;
                }
                String palavraResservada = palavra.toString();
                if (palavraResservada.equals("int") || palavraResservada.equals("float") || palavraResservada.equals("if") ||
                    palavraResservada.equals("else") || palavraResservada.equals("while") || palavraResservada.equals("print")) {
                    tokens.add(palavraResservada);
                } else {
                    tokens.add("ID:" + palavraResservada);  // Identificador
                }
            } else if (Character.isDigit(charAtual)) {  // Números
                StringBuilder numero = new StringBuilder();
                while (i < entrada.length() && Character.isDigit(entrada.charAt(i))) {
                    numero.append(entrada.charAt(i));
                    i++;
                }
                tokens.add("NUM:" + numero.toString());
            } else if (charAtual == '=' || charAtual == '+' || charAtual == '-' || charAtual == '*' || charAtual == '/' || 
                       charAtual == '<' || charAtual == '>' || charAtual == '{' || charAtual == '}' || 
                       charAtual == '(' || charAtual == ')') {  // Operadores e delimitadores
                tokens.add(String.valueOf(charAtual));
                i++;
            } else if (charAtual == '#') {  // Comentários
                while (i < entrada.length() && entrada.charAt(i) != '\n') i++;
            } else {
                i++;  // Ignorar espaços em branco e outros caracteres
            }
        }
        return tokens;
    }

    private EstadoParser transitar(EstadoParser estado, String token) {
        switch (estado) {
            case Q0:
                if (token.startsWith("ID:")) return EstadoParser.Q1;  // Identificador
                if (token.equals("if")) return EstadoParser.Q_IF;  // Início de if
                if (token.equals("while")) return EstadoParser.Q_WHILE;  // Início de while
                if (token.equals("print")) return EstadoParser.Q4;  // Comando de print
                break;

            case Q1:
                if (token.equals("=")) return EstadoParser.Q2;  // Atribuição
                break;

            case Q2:
                if (token.startsWith("NUM:") || token.startsWith("ID:")) return EstadoParser.Q3;  // Atribuição de número ou identificador
                break;

            case Q3:
                if (token.equals(";")) return EstadoParser.Q0;  // Final da instrução de atribuição
                break;

            case Q_IF:
                if (token.equals("(")) return EstadoParser.Q_BLOCK_CONDITION;  // Bloco de condição (abertura de parêntese)
                break;

            case Q_BLOCK_CONDITION:
                if (token.startsWith("ID:") || token.startsWith("NUM:")) return EstadoParser.Q_COND_EXPR;  // Expressão condicional
                break;

            case Q_COND_EXPR:
                if (token.equals(")")) return EstadoParser.Q_BLOCK_START;  // Fecha parêntese da condição
                break;

            case Q_BLOCK_START:
                if (token.equals("{")) return EstadoParser.Q_BLOCK;  // Início de um bloco de código
                break;

            case Q_BLOCK:
                if (token.equals("}")) return EstadoParser.Q0;  // Fim do bloco de código
                if (token.startsWith("ID:")) return EstadoParser.Q1;  // Nova declaração de variável ou comando dentro do bloco
                if (token.equals("print")) return EstadoParser.Q4;  // Comando de print dentro do bloco
                break;

            case Q_WHILE:
                if (token.equals("(")) return EstadoParser.Q_BLOCK_CONDITION;  // Bloco de condição de while
                break;

            case Q4:
                if (token.startsWith("ID:")) return EstadoParser.Q5;  // Identificador a ser impresso
                break;

            case Q5:
                if (token.equals(";")) return EstadoParser.Q0;  // Final da instrução de print
                break;

            default:
                return null;
        }
        return null;
    }

    private boolean processar(String entrada) {
        // Formatando a entrada para remover múltiplos espaços
        entrada = formatarEntrada(entrada);

        EstadoParser estadoAtual = EstadoParser.Q0;  // Estado inicial
        EstadoParser[] estadosFinais = {EstadoParser.Q0};  // Estados finais

        List<String> tokens = tokenizar(entrada);
        for (String token : tokens) {
            estadoAtual = transitar(estadoAtual, token);
            if (estadoAtual == null)
                return false;
        }

        // Se parou num estado final, o código-fonte é bem formado
        for (EstadoParser estadoFinal : estadosFinais) {
            if (estadoAtual == estadoFinal)
                return true;
        }
        return false;
    }
}
