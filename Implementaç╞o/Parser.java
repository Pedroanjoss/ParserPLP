import java.io.*;
import java.util.*;

// Enum para os tipos de tokens que o analisador léxico irá gerar
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

// Classe que representa um token
class Token {
    TokenType tipo;
    String valor;

    Token(TokenType tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}

public class Parser {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Parser <nome-do-arquivo>");
            return;
        }

        String nomeArquivo = args[0];
        StringBuilder entrada = new StringBuilder();  // Utiliza StringBuilder

        Parser p = new Parser();

        try (BufferedReader leitor = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null)
                entrada.append(linha).append("\n");  // Usa append para maior eficiência

        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return;
        }

        boolean resultado = p.processar(entrada.toString());
        System.out.println("Entrada " + (resultado ? "válida" : "inválida"));
    }

    // Função que remove espaços em branco e novas linhas, exceto quando necessário
    private String removerEspacos(String entrada) {
        return entrada.replaceAll("\\s+", " ").trim();  // Substitui múltiplos espaços
    }

    private List<Token> tokenizar(String entrada) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < entrada.length()) {
            char charAtual = entrada.charAt(i);

            if (Character.isWhitespace(charAtual)) {
                i++;  // Ignora espaços em branco
                continue;
            }

            if (charAtual == '#') {  // Início de um comentário
                while (i < entrada.length() && entrada.charAt(i) != '\n') {
                    i++; // Ignora o comentário até o fim da linha
                }
                tokens.add(new Token(TokenType.COMENTARIO, "# Comentário"));
                continue;
            }

            if (Character.isLetter(charAtual)) {  // Identificador
                StringBuilder identificador = new StringBuilder();
                while (i < entrada.length() && (Character.isLetter(entrada.charAt(i)) || Character.isDigit(entrada.charAt(i)))) {
                    identificador.append(entrada.charAt(i));
                    i++;
                }
                String id = identificador.toString();
                switch (id) {
                    case "print":
                        tokens.add(new Token(TokenType.INST_PRINT, id));
                        break;
                    case "if":
                        tokens.add(new Token(TokenType.INST_IF, id));
                        break;
                    case "else":
                        tokens.add(new Token(TokenType.INST_ELSE, id));
                        break;
                    case "while":
                        tokens.add(new Token(TokenType.INST_WHILE, id));
                        break;
                    case "int":
                        tokens.add(new Token(TokenType.TIPO_INT, id));
                        break;
                    case "float":
                        tokens.add(new Token(TokenType.TIPO_FLOAT, id));
                        break;
                    default:
                        tokens.add(new Token(TokenType.IDENT, id));
                }
            } else if (Character.isDigit(charAtual)) {  // Número inteiro
                StringBuilder numero = new StringBuilder();
                while (i < entrada.length() && Character.isDigit(entrada.charAt(i))) {
                    numero.append(entrada.charAt(i));
                    i++;
                }
                tokens.add(new Token(TokenType.NUM_INT, numero.toString()));
            } else if (charAtual == '=') {
                tokens.add(new Token(TokenType.OP_ATR, "="));
                i++;
            } else if (charAtual == '+') {
                tokens.add(new Token(TokenType.OP_SOM, "+"));
                i++;
            } else if (charAtual == ';') {
                tokens.add(new Token(TokenType.PONTO_VIRGULA, ";"));
                i++;
            } else if (charAtual == '{') {
                tokens.add(new Token(TokenType.ABR_CHA, "{"));
                i++;
            } else if (charAtual == '}') {
                tokens.add(new Token(TokenType.FEC_CHA, "}"));
                i++;
            } else if (charAtual == '<') {
                if (i + 1 < entrada.length() && entrada.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.OP_REL_MEIG, "<="));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.OP_REL_MEN, "<"));
                    i++;
                }
            } else if (charAtual == '>') {
                if (i + 1 < entrada.length() && entrada.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.OP_REL_MAIG, ">="));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.OP_REL_MAI, ">"));
                    i++;
                }
            } else {
                i++; // Ignora outros caracteres
            }
        }
        return tokens;
    }

    private Estado transitar(Estado estado, Token token) {
        switch (estado) {
            case Q0:
                if (token.tipo == TokenType.INST_IF) {
                    return Estado.Q1;  // Transição para o estado de instrução if
                } else if (token.tipo == TokenType.INST_WHILE) {
                    return Estado.Q2;  // Transição para o estado de instrução while
                } else if (token.tipo == TokenType.INST_PRINT) {
                    return Estado.Q3;  // Transição para o estado de print
                }
                break;
            case Q1: // Estado para instrução if
                if (token.tipo == TokenType.ABR_PAR) {
                    return Estado.Q4;  // Espera um '(' após if
                }
                break;
            case Q4: // Espera a condição
                if (token.tipo == TokenType.NUM_INT || token.tipo == TokenType.IDENT || token.tipo == TokenType.OP_REL_MAI || token.tipo == TokenType.OP_REL_MEIG || token.tipo == TokenType.OP_REL_MEN) {
                    return Estado.Q5;  // Transição para a verificação da condição
                }
                break;
            case Q5: // Estado após a condição
                if (token.tipo == TokenType.FEC_PAR) {
                    return Estado.Q6;  // Espera um ')' após a condição
                }
                break;
            case Q6: // Estado após a condição fechada
                if (token.tipo == TokenType.ABR_CHA) {
                    return Estado.Q7;  // Espera uma chave aberta para o bloco de instrução
                }
                break;
            case Q2: // Estado para instrução while
                if (token.tipo == TokenType.ABR_PAR) {
                    return Estado.Q8;  // Transição para condição do while
                }
                break;
            // Continuar as outras transições baseadas nos tokens esperados
        }
        return Estado.ERRO; // Se nenhuma condição for satisfeita, retorna erro
    }

    private boolean processar(String entrada) {
        entrada = removerEspacos(entrada);
        List<Token> tokens = tokenizar(entrada);

        Estado estado = Estado.Q0;
        for (Token token : tokens) {
            estado = transitar(estado, token);
            if (estado == Estado.ERRO) {
                return false;
            }
        }
        return estado == Estado.Q0 || estado == Estado.Q7 || estado == Estado.Q12;  // Estado final aceitável
    }

    enum Estado {
        Q0,  // Estado inicial
        Q1,  // Estado para instrução if
        Q2,  // Estado para instrução while
        Q3,  // Estado para instrução print
        Q4,  // Espera '(' após if ou while
        Q5,  // Estado para expressão condicional
        Q6,  // Espera ')' após a condição
        Q7,  // Bloco de instrução (abre chaves)
        Q8,  // Estado para expressão while
        Q12, // Estado final (instrução concluída)
        ERRO // Estado de erro
    }
}