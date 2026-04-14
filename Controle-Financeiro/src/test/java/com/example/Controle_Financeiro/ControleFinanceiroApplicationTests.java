package com.example.Controle_Financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ControleFinanceiroApplicationTests {

	@Test
	void contextLoads() {

		List<Integer> numeros = new ArrayList<Integer>();

		for(int i=1; i<27;i++){
			numeros.add(i);
		}
		int j=0;
		System.out.println("Valores Inicial: "+numeros.toString());
		while(numeros.size()>1){
				j = (j + 3) % numeros.size();
					numeros.remove(j);
					System.out.println("Valor dentro do for: "+ numeros.toString());
		}
		System.out.println(numeros.toString());



	}

}
