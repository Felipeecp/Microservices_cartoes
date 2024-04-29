package io.github.felipeecp.mscartoes.application;

import io.github.felipeecp.mscartoes.domain.Cartao;
import io.github.felipeecp.mscartoes.domain.ClienteCartao;
import io.github.felipeecp.mscartoes.representation.CartaoSaveRequest;
import io.github.felipeecp.mscartoes.representation.CartoesPorClienteResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("cartoes")
@RequiredArgsConstructor
public class CartoesController {

    private final CartaoService cartaoService;
    private final ClienteCartaoService clienteCartaoService;

    @GetMapping("ok")
    public String ok() {
        return "ok";
    }

    @GetMapping
    public ResponseEntity<List<Cartao>> findAll(){
        List<Cartao> allCartoes = cartaoService.getAllCartoes();
        return ResponseEntity.ok(allCartoes);
    }

    @PostMapping
    public ResponseEntity cadastra(@RequestBody CartaoSaveRequest request){
        Cartao cartao = request.toModel();
        cartaoService.save(cartao);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(params = "renda")
    public ResponseEntity<List<Cartao>> getCartoesRendaMaxima(@RequestParam("renda") Long renda){
        List<Cartao> listCatoes = cartaoService.getCartoesRendaMenorIgual(renda);
        return ResponseEntity.ok(listCatoes);
    }

    @GetMapping("cpf")
    public ResponseEntity<List<CartoesPorClienteResponse>> getCatoesByCliente(@RequestParam("cpf") String cpf){
        List<ClienteCartao> listClienteCartoes = clienteCartaoService.listCartoesByCpf(cpf);
        List<CartoesPorClienteResponse> resultList = listClienteCartoes.stream().map(CartoesPorClienteResponse::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultList);
    }

}
