import java.util.ArrayList;
import java.util.List;

// Enum para os tipos de tokens que o parser irá reconhecer diretamente
enum TokenType {
    IDENT,      // Identificadores (variáveis, funções)
    NUM_INT,    // Números inteiros
    NUM_REA,    // Números reais
    OP_ATR,     // Operador de atribuição (=)
    OP_SOM,     // Operador de soma (+)
    OP_REL_MAI, // Operador relacional maior (>)
    OP_REL_MEIG,// Operador relacional menor ou igual (<=)
    OP_LOG_E,   // Operador lógico E (&&)
    ABR_PAR,    // Abre parênteses '('
    FEC_PAR,    // Fecha parênteses ')'
    ABR_CHA,    // Abre chave '{'
    FEC_CHA,    // Fecha chave '}'
    TIPO_INT,   // Tipo int
    TIPO_FLOAT, // Tipo float
    VIRGULA,    // Vírgula ','
    PONTO_VIRGULA, // Ponto e vírgula ';'
    INST_IF,    // Instrução "if"
    INST_ELSE,  // Instrução "else"
    INST_WHILE, // Instrução "while"
    INST_PRINT  // Instrução "print"
}

// Classe que representa cada token
class Token {
    private TokenType tipo;
    private String lexema;

    public Token(TokenType tipo, String lexema) {
        this.tipo = tipo;
        this.lexema = lexema;
    }

    public TokenType getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    @Override
    public String toString() {
        return tipo + " " + lexema;
    }
}

// Estados do parser sintático
enum EstadoParser {
    Q0, Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9, // Adicione mais estados conforme necessário
}

// Analisador Sintático responsável por verificar a validade da sequência de tokens
class Parser {
    public boolean processar(String codigo) {
        EstadoParser estadoAtual = EstadoParser.Q0;

        List<Token> tokens = analisarTokens(codigo); // Agora direto da função de análise

        for (Token token : tokens) {
            estadoAtual = transitar(estadoAtual, token);
            if (estadoAtual == null) {
                return false; // Se não houver transição válida, o código é inválido
            }
        }

        return estadoAtual == EstadoParser.Q0; // Verifique se terminamos em um estado de aceitação
    }

    // Função para reconhecer tokens diretamente da entrada
    private List<Token> analisarTokens(String codigo) {
        List<Token> tokens = new ArrayList<>();

        String[] palavras = codigo.split("\\s+");
        
        for (String palavra : palavras) {
            switch (palavra) {
                case "if":
                    tokens.add(new Token(TokenType.INST_IF, palavra));
                    break;
                case "else":
                    tokens.add(new Token(TokenType.INST_ELSE, palavra));
                    break;
                case "while":
                    tokens.add(new Token(TokenType.INST_WHILE, palavra));
                    break;
                case "print":
                    tokens.add(new Token(TokenType.INST_PRINT, palavra));
                    break;
                case "int":
                    tokens.add(new Token(TokenType.TIPO_INT, palavra));
                    break;
                case "float":
                    tokens.add(new Token(TokenType.TIPO_FLOAT, palavra));
                    break;
                case "=":
                    tokens.add(new Token(TokenType.OP_ATR, palavra));
                    break;
                case ">":
                    tokens.add(new Token(TokenType.OP_REL_MAI, palavra));
                    break;
                case "<=":
                    tokens.add(new Token(TokenType.OP_REL_MEIG, palavra));
                    break;
                case "&&":
                    tokens.add(new Token(TokenType.OP_LOG_E, palavra));
                    break;
                case ";":
                    tokens.add(new Token(TokenType.PONTO_VIRGULA, palavra));
                    break;
                case ",":
                    tokens.add(new Token(TokenType.VIRGULA, palavra));
                    break;
                case "(":
                    tokens.add(new Token(TokenType.ABR_PAR, palavra));
                    break;
                case ")":
                    tokens.add(new Token(TokenType.FEC_PAR, palavra));
                    break;
                case "{":
                    tokens.add(new Token(TokenType.ABR_CHA, palavra));
                    break;
                case "}":
                    tokens.add(new Token(TokenType.FEC_CHA, palavra));
                    break;
                default:
                    if (palavra.matches("\\d+")) {
                        tokens.add(new Token(TokenType.NUM_INT, palavra));
                    } else if (palavra.matches("\\d+\\.\\d+")) {
                        tokens.add(new Token(TokenType.NUM_REA, palavra));
                    } else {
                        tokens.add(new Token(TokenType.IDENT, palavra));
                    }
            }
        }
        
        return tokens;
    }

    // Transições de estados no parser
    private EstadoParser transitar(EstadoParser estado, Token token) {
        switch (estado) {
            case Q0:
                if (token.getTipo() == TokenType.INST_IF) return EstadoParser.Q1;
                if (token.getTipo() == TokenType.INST_WHILE) return EstadoParser.Q1;
                break;
            case Q1:
                if (token.getTipo() == TokenType.ABR_PAR) return EstadoParser.Q2;
                break;
            case Q2:
                if (token.getTipo() == TokenType.NUM_REA || token.getTipo() == TokenType.NUM_INT || token.getTipo() == TokenType.IDENT) return EstadoParser.Q3;
                break;
            case Q3:
                if (token.getTipo() == TokenType.OP_REL_MAI || token.getTipo() == TokenType.OP_REL_MEIG) return EstadoParser.Q4;
                break;
            case Q4:
                if (token.getTipo() == TokenType.NUM_REA || token.getTipo() == TokenType.NUM_INT || token.getTipo() == TokenType.IDENT) return EstadoParser.Q5;
                break;
            case Q5:
                if (token.getTipo() == TokenType.FEC_PAR) return EstadoParser.Q6;
                break;
            case Q6:
                if (token.getTipo() == TokenType.ABR_CHA) return EstadoParser.Q7;
                break;
            case Q7:
                // Transições para dentro do bloco
                if (token.getTipo() == TokenType.IDENT || token.getTipo() == TokenType.TIPO_INT || token.getTipo() == TokenType.TIPO_FLOAT) return EstadoParser.Q8;
                break;
            case Q8:
                if (token.getTipo() == TokenType.PONTO_VIRGULA) return EstadoParser.Q7; // Continua dentro do bloco
                if (token.getTipo() == TokenType.FEC_CHA) return EstadoParser.Q0; // Fecha o bloco
                break;
            // Adicione outros estados e transições conforme necessário
        }
        return null; // Se não encontrar transição válida, retorna null
    }
}

// Classe principal para testar o analisador sintático diretamente com código-fonte
public class Main {
    public static void main(String[] args) {
        String codigoFonte = "if (4.5 > fator && taxa <= 2.32) { int teste, teste2; a = b + 3; }";

        Parser parser = new Parser();
        boolean resultado = parser.processar(codigoFonte);

        System.out.println("Código fonte " + (resultado ? "válido" : "inválido"));
    }
}
