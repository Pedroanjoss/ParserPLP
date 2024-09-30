import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Enum para os tipos de tokens que o analisador léxico irá gerar
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
    VIRGULA,    // Vírgula ','
    PONTO_VIRGULA, // Ponto e vírgula ';'
    INST_IF     // Instrução "if"
}

// Classe que representa cada token gerado pelo analisador léxico
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

// Representa a tabela de símbolos usada pelo analisador léxico para armazenar tokens
class TabelaSimbolos {
    private Map<String, Token> tabela = new HashMap<>();

    public void adicionarSimbolo(String lexema, TokenType tipo) {
        tabela.put(lexema, new Token(tipo, lexema));
    }

    public Token buscarSimbolo(String lexema) {
        return tabela.get(lexema);
    }

    public void imprimirTabela() {
        tabela.forEach((lexema, token) -> {
            System.out.println("Lexema: " + lexema + ", Token: " + token);
        });
    }
}

// Analisador Léxico responsável por realizar a análise léxica e gerar tokens a partir do código-fonte
class AnalisadorLexico {
    private TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();

    public List<Token> analisar(String codigo) {
        List<Token> tokens = new ArrayList<>();
        
        // Exemplo simples de reconhecimento de alguns tokens.
        // O ideal é substituir isso por um mecanismo de varredura mais robusto (regex ou autômatos).
        
        String[] palavras = codigo.split("\\s+");
        
        for (String palavra : palavras) {
            switch (palavra) {
                case "if":
                    tokens.add(new Token(TokenType.INST_IF, palavra));
                    tabelaSimbolos.adicionarSimbolo(palavra, TokenType.INST_IF);
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
                case "int":
                    tokens.add(new Token(TokenType.TIPO_INT, palavra));
                    tabelaSimbolos.adicionarSimbolo(palavra, TokenType.TIPO_INT);
                    break;
                case "=":
                    tokens.add(new Token(TokenType.OP_ATR, palavra));
                    break;
                case ">":
                    tokens.add(new Token(TokenType.OP_REL_MAI, palavra));
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
                default:
                    if (palavra.matches("\\d+")) {
                        tokens.add(new Token(TokenType.NUM_INT, palavra));
                    } else if (palavra.matches("\\d+\\.\\d+")) {
                        tokens.add(new Token(TokenType.NUM_REA, palavra));
                    } else {
                        tokens.add(new Token(TokenType.IDENT, palavra));
                        tabelaSimbolos.adicionarSimbolo(palavra, TokenType.IDENT);
                    }
            }
        }
        
        return tokens;
    }

    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }
}

// Estados do parser sintático
enum EstadoParser {
    Q0, Q1, Q2, Q3, Q4, // Adicione mais estados conforme necessário
}

// Analisador Sintático responsável por verificar a validade da sequência de tokens
class Parser {
    public boolean processar(List<Token> tokens) {
        EstadoParser estadoAtual = EstadoParser.Q0;

        for (Token token : tokens) {
            estadoAtual = transitar(estadoAtual, token);
            if (estadoAtual == null) {
                return false; // Se não houver transição válida, o código é inválido
            }
        }

        return estadoAtual == EstadoParser.Q0; // Verifique se terminamos em um estado de aceitação
    }

    private EstadoParser transitar(EstadoParser estado, Token token) {
        switch (estado) {
            case Q0:
                if (token.getTipo() == TokenType.INST_IF) return EstadoParser.Q1;
                break;
            case Q1:
                if (token.getTipo() == TokenType.ABR_PAR) return EstadoParser.Q2;
                break;
            case Q2:
                if (token.getTipo() == TokenType.NUM_REA || token.getTipo() == TokenType.NUM_INT) return EstadoParser.Q3;
                break;
            case Q3:
                if (token.getTipo() == TokenType.OP_REL_MAI) return EstadoParser.Q4;
                break;
            case Q4:
                if (token.getTipo() == TokenType.IDENT) return EstadoParser.Q0; // Pode continuar com outros estados
                break;
            // Adicione outros estados e transições conforme necessário
        }
        return null; // Se não encontrar transição válida, retorna null
    }
}

// Classe principal para testar o analisador léxico e sintático
public class Main {
    public static void main(String[] args) {
        String codigoFonte = "if (4.5 > fator && taxa <= 2.32) { int teste, teste2; a = b + 3; }";

        // Análise Léxica
        AnalisadorLexico lexico = new AnalisadorLexico();
        List<Token> tokens = lexico.analisar(codigoFonte);

        System.out.println("Tokens gerados:");
        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println("\nTabela de Símbolos:");
        lexico.getTabelaSimbolos().imprimirTabela();

        // Análise Sintática
        Parser parser = new Parser();
        boolean resultadoSintatico = parser.processar(tokens);

        System.out.println("\nAnálise Sintática: " + (resultadoSintatico ? "Válida" : "Inválida"));
    }
}
