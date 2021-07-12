package br.com.zup.edu

import com.google.protobuf.Any
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.Status
import io.micronaut.http.exceptions.HttpStatusException
import javax.inject.Inject

@Controller
class CalculadoraDeFretesController(@Inject val gRpcClient: FreteZupServiceGrpc.FreteZupServiceBlockingStub) {

    @Get("/api/fretes")
    fun calcula(@QueryValue cep: String): FreteDeResposta {
        val request = FreteRequest.newBuilder()
            .setCep(cep)
            .build()
        try {
            val resposta = gRpcClient.calculaFrete(request)
            return FreteDeResposta(
                resposta.cep,
                resposta.valor
            )
        } catch (e: StatusRuntimeException) {
            val status = e.status.code
            val descricao = e.status.description

            if (status == io.grpc.Status.Code.INVALID_ARGUMENT) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, descricao)
            }

            if (status == io.grpc.Status.Code.PERMISSION_DENIED) {
                val statusProto = StatusProto.fromThrowable(e) //Pega o status code da exception lancada

                if (statusProto == null) {
                    throw HttpStatusException(HttpStatus.FORBIDDEN, descricao)
                }
                val anyDetails: Any = statusProto.detailsList.get(0)
                val erroDetails = anyDetails.unpack(ErroDetails::class.java)
                throw HttpStatusException(HttpStatus.FORBIDDEN, "${erroDetails.code}: ${erroDetails.message}")

            }
            throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, descricao)
        }
    }
}


data class FreteDeResposta(val cep: String, val valor: Double)
