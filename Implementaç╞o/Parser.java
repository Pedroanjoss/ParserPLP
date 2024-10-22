import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

enum TokenType {
    IDENT,          // Identificadores (variáveis, funções)
    NUM_INT,        // Números inteiros
    OP_ATR,         // Operador de atribuição (=)
    OP_SOM,         // Operador de soma (+)
    OP_REL_MAI,     // Operador relacional maior (>)
    OP_REL_MAIG,    // Operador relacional maior ou igual (>=)
    OP_REL_MEN,     // Operador relacional menor (<)
    OP_REL_MEIG,    // Operador relacional menor ou igual (<=)
    ABR_PAR,        // Abre parênteses '('
    FEC_PAR,        // Fecha parênteses ')'
    ABR_CHA,        // Abre chave '{'
    FEC_CHA,        // Fecha chave '}'
    TIPO_INT,       // Tipo int
    TIPO_FLOAT,     // Tipo float
    VIRGULA,        // Vírgula ','
    PONTO_VIRGULA,  // Ponto e vírgula ';'
    INST_IF,        // Instrução "if"
    INST_ELSE,      // Instrução "else"
    INST_WHILE,     // Instrução "while"
    INST_PRINT,     // Instrução "print"
    COMENTARIO      // Comentário
}

enum Estado {
    Q0,    // Estado inicial e de aceitação
    Q1,    // Após ler um identificador
    Q2,    // Após ler '='
    Q3,    // Após ler um número
    Q4,    // Após ler 'print'
    Q5,    // Após ler um identificador para 'print'
    Q6,    // Para operadores relacionais
    Q7,    // Para operadores de soma
    Q8,    // Para abrir parênteses
    Q9,    // Para fechar parênteses
    Q10,   // Para abrir chaves
    Q11,   // Para fechar chaves
    Q12,   // Para tipos como int e float
    Q13,   // Para palavras-chave como 'if' e 'while'
    Q14,   // Para ponto e vírgula
    Q15,   // Para comentários
    ERRO   // Estado de erro
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

    private List<String> tokenizar(String entrada) {
        List<String> tokens = new ArrayList<>();
        int i = 0;

        while (i < entrada.length()) {
            char charAtual = entrada.charAt(i);

            if (Character.isLetter(charAtual)) {  // Identificadores e palavras-chave
                StringBuilder identificador = new StringBuilder();
                while (i < entrada.length() && Character.isLetter(entrada.charAt(i))) {
                    identificador.append(entrada.charAt(i));
                    i++;
                }
                String token = identificador.toString();
                if (token.equals("int") || token.equals("float")) {
                    tokens.add("TIPO_" + token.toUpperCase());
                } else if (token.equals("if") || token.equals("else") || token.equals("while") || token.equals("print")) {
                    tokens.add("INST_" + token.toUpperCase());
                } else {
                    tokens.add("IDENT");
                }
            } else if (Character.isDigit(charAtual)) {  // Números
                StringBuilder numero = new StringBuilder();
                while (i < entrada.length() && Character.isDigit(entrada.charAt(i))) {
                    numero.append(entrada.charAt(i));
                    i++;
                }
                tokens.add("NUM_INT");
            } else if (charAtual == '=') {
                tokens.add("OP_ATR");
                i++;
            } else if (charAtual == '+') {
                tokens.add("OP_SOM");
                i++;
            } else if (charAtual == '>') {
                if (i + 1 < entrada.length() && entrada.charAt(i + 1) == '=') {
                    tokens.add("OP_REL_MAIG");
                    i += 2;
                } else {
                    tokens.add("OP_REL_MAI");
                    i++;
                }
            } else if (charAtual == '<') {
                if (i + 1 < entrada.length() && entrada.charAt(i + 1) == '=') {
                    tokens.add("OP_REL_MEIG");
                    i += 2;
                } else {
                    tokens.add("OP_REL_MEN");
                    i++;
                }
            } else if (charAtual == '(') {
                tokens.add("ABR_PAR");
                i++;
            } else if (charAtual == ')') {
                tokens.add("FEC_PAR");
                i++;
            } else if (charAtual == '{') {
                tokens.add("ABR_CHA");
                i++;
            } else if (charAtual == '}') {
                tokens.add("FEC_CHA");
                i++;
            } else if (charAtual == ',') {
                tokens.add("VIRGULA");
                i++;
            } else if (charAtual == ';') {
                tokens.add("PONTO_VIRGULA");
                i++;
            } else if (charAtual == '#') {  // Comentário de uma linha
                while (i < entrada.length() && entrada.charAt(i) != '\n') {
                    i++;
                }
                tokens.add("COMENTARIO");
            } else {
                i++;
            }
        }

        return tokens;
    }

    private Estado transitar(Estado estado, String token) {
        switch (estado) {
            case Q0:
                if (token.equals("INST_PRINT")) {
                    return Estado.Q4;
                } else if (token.equals("IDENT")) {
                    return Estado.Q1;
                } else if (token.equals("INST_IF") || token.equals("INST_WHILE")) {
                    return Estado.Q13;
                }
                break;
            case Q1:
                if (token.equals("OP_ATR")) {
                    return Estado.Q2;
                }
                break;
            case Q2:
                if (token.equals("NUM_INT")) {
                    return Estado.Q3;
                }
                break;
            case Q3:
                if (token.equals("PONTO_VIRGULA")) {
                    return Estado.Q0;
                }
                break;
            case Q4:
                if (token.equals("IDENT")) {
                    return Estado.Q5;
                }
                break;
            case Q5:
                if (token.equals("PONTO_VIRGULA")) {
                    return Estado.Q0;
                }
                break;
            case Q13:
                if (token.equals("ABR_PAR")) {
                    return Estado.Q8;
                }
                break;
            case Q8:
                if (token.equals("IDENT") || token.equals("NUM_INT")) {
                    return Estado.Q9;
                }
                break;
            case Q9:
                if (token.equals("FEC_PAR")) {
                    return Estado.Q10;
                }
                break;
            case Q10:
                if (token.equals("ABR_CHA")) {
                    return Estado.Q0;
                }
                break;
            default:
                return Estado.ERRO;
        }
        return Estado.ERRO;
    }

    private boolean processar(String entrada) {
        Estado estadoAtual = Estado.Q0;
        Estado[] estadosFinais = {Estado.Q0};

        List<String> tokens = tokenizar(entrada);
        for (String token : tokens) {
            estadoAtual = transitar(estadoAtual, token);
            if (estadoAtual == Estado.ERRO)
                return false;
        }

        for (Estado estadoFinal : estadosFinais) {
            if (estadoAtual == estadoFinal)
                return true;
        }
        return false;
    }
}
