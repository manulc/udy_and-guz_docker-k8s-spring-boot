package org.mlorenzo.springcloud.msvc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// Opcional porque basta con tener la dependencia spring-cloud-starter-kubernetes-client para que se habilite el
// descubrimiento
@EnableDiscoveryClient
@SpringBootApplication
public class MsvcGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcGatewayApplication.class, args);
	}

}
