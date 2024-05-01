package io.github.felipeecp.msavaliadorcredito.application.exceptions;

import lombok.Getter;

public class ErroComunicacaoMicroserviceException extends Exception{

    @Getter
    private Integer status;

    public ErroComunicacaoMicroserviceException(String mensagem, Integer status){
        super(mensagem);
        this.status = status;
    }
}
