package io.github.felipeecp.msavaliadorcredito.application.exceptions;



public class ErroSolicitacaoCartaoException extends RuntimeException {
    public ErroSolicitacaoCartaoException(String mensagem) {
        super(mensagem);
    }
}
