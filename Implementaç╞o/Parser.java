import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

enum TokenType {
    IDENT, // Identificadores (variáveis, funções)
    NUM_INT, // Números inteiros
    NUM_FLOAT, // Números decimais (float)
    OP_ATR, // Operador de atribuição (=)
    OP_SOM, // Operador de soma (+)
    OP_SUB, // Operador de subtração (-)
    OP_MUL, // Operador de multiplicação (*)
    OP_DIV, // Operador de divisão (/)
    OP_REL_MAI, // Operador relacional maior (>)
    OP_REL_MAIG, // Operador relacional maior ou igual (>=)
    OP_REL_MEN, // Operador relacional menor (<)
    OP_REL_MEIG, // Operador relacional menor ou igual (<=)
    ABR_PAR, // Abre parênteses '('
    FEC_PAR, // Fecha parênteses ')'
    ABR_CHA, // Abre chave '{'
    FEC_CHA, // Fecha chave '}'
    TIPO_INT, // Tipo int
    TIPO_FLOAT, // Tipo float
    VIRGULA, // Vírgula ','
    PONTO_VIRGULA, // Ponto e vírgula ';'
    INST_IF, // Instrução "if"
    INST_ELSE, // Instrução "else"
    INST_WHILE, // Instrução "while"
    INST_PRINT, // Instrução "print"
    COMENTARIO, // Comentário
    COMENTARIO_MULTI // Comentário multilinha
}

enum Estado {
    Q0, // Estado inicial e de aceitação
    Q1, // Após ler um identificador
    Q2, // Após ler '='
    Q3, // Após ler um número
    Q4, // Após ler 'print'
    Q5, // Após ler 'print' com parênteses
    Q6, // Para operadores relacionais
    Q7, // Para operadores de soma
    Q8, // Para abrir parênteses
    Q9, // Para fechar parênteses
    Q10, // Para abrir chaves '{'
    Q11, // Para fechar chaves '}'
    Q12, // Para tipos como int e float
    Q13, // Para palavras-chave como 'if' e 'while'
    Q14, // Para expressões dentro de parênteses
    Q15, // Para ponto e vírgula
    Q16, // Para operadores relacionais após um identificador
    ERRO // Estado de erro
}

public class Parser {

    public static void main(String[] args) {
        args = new String[] { "Implementaç╞o/programa2.plp" };
        if (args.length != 1) {
            args[0] = ("programa2.plp");
            return;
        }

        String nomeArquivo = args[0];
        String entrada = "";

        Parser p = new Parser();

        try (BufferedReader leitor = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                entrada += linha + "\n"; // Mantém a nova linha
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return;
        }

        boolean resultado = p.processar(entrada);
        System.out.println("Entrada " + (resultado ? "válida" : "inválida"));
    }

    private List<String> tokenizar(String entrada) {
        List<String> tokens = new ArrayList<>();
        int i = 0;

        while (i < entrada.length()) {
            char charAtual = entrada.charAt(i);

            // Ignora espaços em branco
            if (Character.isWhitespace(charAtual)) {
                i++;
                continue;
            }

            // Identificadores e palavras-chave
            if (Character.isLetter(charAtual)) {
                StringBuilder identificador = new StringBuilder();
                while (i < entrada.length() && Character.isLetter(entrada.charAt(i))) {
                    identificador.append(entrada.charAt(i));
                    i++;
                }
                String token = identificador.toString();
                if (token.equals("int") || token.equals("float")) {
                    tokens.add("TIPO_" + token.toUpperCase());
                } else if (token.equals("if") || token.equals("else") || token.equals("while")
                        || token.equals("print")) {
                    tokens.add("INST_" + token.toUpperCase());
                } else {
                    tokens.add("IDENT");
                }
            }
            // Números inteiros ou decimais
            else if (Character.isDigit(charAtual)) {
                StringBuilder numero = new StringBuilder();
                boolean temPonto = false;
                while (i < entrada.length() && (Character.isDigit(entrada.charAt(i)) || entrada.charAt(i) == '.')) {
                    if (entrada.charAt(i) == '.') {
                        if (temPonto)
                            break;
                        temPonto = true;
                    }
                    numero.append(entrada.charAt(i));
                    i++;
                }
                tokens.add(temPonto ? "NUM_FLOAT" : "NUM_INT");
            }
            // Operadores relacionais
            else if (charAtual == '>') {
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
            }
            // Operadores e outros símbolos
            else if (charAtual == '=') {
                tokens.add("OP_ATR");
                i++;
            } else if (charAtual == '+') {
                tokens.add("OP_SOM");
                i++;
            } else if (charAtual == '-') {
                tokens.add("OP_SUB");
                i++;
            } else if (charAtual == '*') {
                tokens.add("OP_MUL");
                i++;
            } else if (charAtual == '/') {
                tokens.add("OP_DIV");
                i++;
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
            } else if (charAtual == ';') {
                tokens.add("PONTO_VIRGULA");
                i++;
            } else {
                System.out.println("Erro: Caractere inesperado '" + charAtual + "' encontrado.");
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
                } else if (token.equals("INST_IF") || token.equals("INST_WHILE") || token.equals("INST_ELSE")) {
                    return Estado.Q13; // Lidar com if e while
                } else if (token.equals("TIPO_INT") || token.equals("TIPO_FLOAT")) {
                    return Estado.Q12; // Espera um identificador após um tipo
                } else if (token.equals("ABR_CHA") || token.equals("FEC_CHA")) { // Se for uma abertura de chaves
                    return Estado.Q0; // Transita para o estado de abertura de chaves
                }

                break;
            case Q1:
                if (token.equals("OP_ATR")) {
                    return Estado.Q2;
                } else if (token.equals("PONTO_VIRGULA")) {
                    return Estado.Q0; // Finaliza a declaração da variável sem valor
                } else if (token.equals("NUM_INT") || token.equals("NUM_FLOAT")) {
                    return Estado.Q2; // Aceita números após IDENT
                } else if (token.startsWith("OP_REL")) { // Operadores relacionais
                    return Estado.Q6; // Transição para o estado de operadores relacionais
                }
                break;
            case Q2:
                if (token.equals("NUM_INT") || token.equals("NUM_FLOAT")) {
                    return Estado.Q3; // Aceita o número e vai para o estado Q3
                } else if (token.equals("PONTO_VIRGULA")) {
                    return Estado.Q0; // Aceita ponto e vírgula diretamente após o operador de atribuição
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
                } else if (token.equals("ABR_PAR")) { // Permite o uso de parênteses com print
                    return Estado.Q8;
                }
                break;
            case Q5:
                if (token.equals("PONTO_VIRGULA")) {
                    return Estado.Q0;
                }
                break;
            case Q8: // Após abrir parênteses no print
                if (token.equals("IDENT") || token.equals("NUM_INT") || token.equals("NUM_FLOAT")) {
                    return Estado.Q9; // Espera o conteúdo do parêntese
                }
                break;
            case Q9: // Após ler um identificador ou número dentro dos parênteses
                if (token.equals("FEC_PAR")) {
                    return Estado.Q0; // Retorna ao estado inicial após fechar parênteses
                } else if (token.equals("PONTO_VIRGULA")) {
                    return Estado.Q0; // Permite que um print finalize sem fechar parênteses
                } else if (token.equals("OP_REL_MAI") || token.equals("OP_REL_MAIG") ||
                        token.equals("OP_REL_MEN") || token.equals("OP_REL_MEIG") ||
                        token.equals("OP_REL_IGUAL") || token.equals("OP_REL_DIF")) { // Se for um operador relacional
                    return Estado.Q15; // Transita para o estado de operadores relacionais
                }
                break;
            case Q12: // Depois de ler um tipo (int ou float)
                if (token.equals("IDENT")) {
                    return Estado.Q1; // Espera um identificador após o tipo
                }
                break;
            case Q13: // Para palavras-chave como 'if', 'while' e 'else'
                if (token.equals("ABR_PAR")) {
                    return Estado.Q14; // Espera abrir parênteses
                } else if (token.equals("INST_ELSE")) {
                    return Estado.Q10; // Espera abrir chave ou instrução após else
                } else if (token.equals("ABR_CHA")) {
                    return Estado.Q0; // Transita para o estado de abrir chaves
                } else {
                    return Estado.ERRO; // Caso contrário, retorna erro
                }
            case Q14: // Lê expressões dentro de parênteses
                if (token.equals("IDENT") || token.equals("NUM_INT") || token.equals("NUM_FLOAT")) {
                    return Estado.Q14; // Espera um número ou identificador após um operador relacional
                } else if (token.equals("OP_REL_MAI") || token.equals("OP_REL_MAIG") ||
                        token.equals("OP_REL_MEN") || token.equals("OP_REL_MEIG") ||
                        token.equals("OP_REL_IGUAL") || token.equals("OP_REL_DIF")) {
                    return Estado.Q15; // Transita para o estado de operadores relacionais
                }
                break;
            case Q15: // Após ler um operador relacional
                if (token.equals("NUM_INT") || token.equals("NUM_FLOAT") || token.equals("IDENT")) {
                    return Estado.Q9; // Espera um número ou identificador antes de fechar parênteses
                }
                break;
            case Q10: // abre chaves
                if (token.equals("NUM_INT") || token.equals("NUM_FLOAT")) {
                    return Estado.Q2; // Volta para o estado inicial após abrir chave
                } else {
                    return Estado.Q0; // Volta para o estado inicial após fechar chave
                }

            default:
                return Estado.ERRO;
        }
        return Estado.ERRO;
    }

    private boolean processar(String entrada) {
        Estado estadoAtual = Estado.Q0;
        Estado[] estadosFinais = { Estado.Q0 };

        List<String> tokens = tokenizar(entrada);
        if (tokens.isEmpty()) {
            return false; // Retorna false se a tokenização falhou
        }

        for (String token : tokens) {
            estadoAtual = transitar(estadoAtual, token);
            if (estadoAtual == Estado.ERRO) {
                return false;
            }
        }

        for (Estado estadoFinal : estadosFinais) {
            if (estadoAtual == estadoFinal) {
                return true;
            }
        }
        return false;
    }
}