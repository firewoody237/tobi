package com.example.tobi.integrated.db.service

import com.example.tobi.integrated.common.resultcode.ResultCode
import com.example.tobi.integrated.common.resultcode.ResultCodeException
import com.example.tobi.integrated.db.dto.GetItemDTO
import com.example.tobi.integrated.db.dto.GetUserDTO
import com.example.tobi.integrated.db.model.Item
import com.example.tobi.integrated.db.model.User
import io.netty.channel.ChannelOption
import io.netty.channel.ConnectTimeoutException
import net.sf.json.JSONObject
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.time.Duration

@Service
class ProductApiService(
    @Value("\${product.api.host}")
    private val PRODUCT_API_HOST: String,
    @Value("\${product.api.proxy}")
    private val PRODUCT_API_PROXY: String,
    @Value("\${product.api.getItemById}")
    private val PRODUCT_API_GET_ITEM: String,


    ) {

    companion object {
        private val log = LogManager.getLogger()
    }


    private val httpClient = HttpClient
        .create()
        .baseUrl(PRODUCT_API_HOST)
        .responseTimeout(Duration.ofMillis(5000))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)

    private val webClient = WebClient
        .builder()
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .build()

    fun getItemById(itemId: Long?): Item {
        if (itemId == null) {
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_PARAMETER_TYPE,
                loglevel = Level.INFO
            )
        }

        val uri = PRODUCT_API_PROXY + PRODUCT_API_GET_ITEM
        try {
            val result = webClient.method(HttpMethod.GET)
                .uri(uri)
                .header("requestUuid", ThreadContext.get("requestUuid"))
                .body(Mono.just(GetItemDTO(id = itemId)), GetItemDTO::class.java)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JSONObject::class.java)
                .retryWhen(
                    Retry.max(2).filter {
                        it.cause is ConnectTimeoutException
                    }
                )
                .block() ?: throw ResultCodeException(
                resultCode = ResultCode.ERROR_ITEM_CONNECTION,
                loglevel = Level.INFO,
            )

            log.info("get Item by Id. uri: $uri, result: $result")
            if (result["rtncd"] != 1000) {
                //실패
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_ITEM_RESPONSE,
                    loglevel = Level.INFO,
                    message = "[${result.get("rtncd")}] ${result.get("rtnmsg")}"
                )
            }

            val response =
                try {
                    result["response"] as JSONObject
                } catch (e: Exception) {
                    throw ResultCodeException(
                        resultCode = ResultCode.ERROR_ITEM_RESPONSE,
                        loglevel = Level.WARN,
                        message = "response is null"
                    )
                }


            val id = try {
                response["id"].toString().toLong()
            } catch (e: Exception) {
                throw ResultCodeException(
                    resultCode = ResultCode.ERROR_ITEM_RESPONSE,
                    loglevel = Level.WARN,
                    message = "id is null"
                )
            }

            val name = response["name"] as String
            val price = response["price"].toString().toLong()
            val contentProvider = response["contentProvider"] as String
            val category = response["category"] as String


            return Item(
                name = name,
                price = price,
                contentProvider = contentProvider,
                category = category
            )
        } catch (e: ResultCodeException) {
            throw e
        } catch (e: Exception) {
            log.error("get Item error. uri: $uri", e)
            throw ResultCodeException(
                resultCode = ResultCode.ERROR_ITEM_RESPONSE,
                loglevel = Level.INFO
            )
        }
    }
}