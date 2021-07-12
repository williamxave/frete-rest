package br.com.zup.edu

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import javax.inject.Inject

@Controller
class CalculadoraDeFretesController(@Inject val gRpcClient: FreteZupServiceGrpc.FreteZupServiceBlockingStub) {

    @Get("/api/fretes")
    fun calcula(@QueryValue cep: String): FreteDeResposta {

        val request = FreteRequest.newBuilder()
            .setCep(cep)
            .build()
        val resposta = gRpcClient.calculaFrete(request)
        return FreteDeResposta(resposta.cep, resposta.valor)
    }
}

data class FreteDeResposta(val cep: String, val valor: Double)
