package jv.supermarket;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = SupermarketApplication.class)
@AutoConfigureMockMvc
@Transactional
public class SupermarketApplicationTests {

	@Autowired
	MockMvc mvc;

	private final String baseUrl = "/supermarket/";

	@Test
	public void testProductWithoutAuth_Forbidden() throws Exception {
		mvc.perform(get(baseUrl + "product/all"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testProductWithAuth() throws Exception {
		mvc.perform(get(baseUrl + "product/all"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testProductGetWithAuth() throws Exception {
		mvc.perform(get(baseUrl + "product/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Smartphone"))
				.andExpect(jsonPath("$.brand").value("Samsung"))
				.andExpect(jsonPath("$.price").value(3000.00))
				.andExpect(jsonPath("$.description").value("O melhor da Samsung"))
				.andExpect(jsonPath("$.categories").isArray())
				.andExpect(jsonPath("$.categories", org.hamcrest.Matchers.containsInAnyOrder("Smartphones", "Eletrônicos")))
				.andExpect(jsonPath("$.images").isArray());
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testSaveProductWithAuth() throws Exception {
		String productJson = """
				    {
				        "name": "Smartphone",
				        "brand": "LG",
				        "price": 3000,
				        "stock": 20,
				        "description": "O melhor da LG",
				        "categories": ["Smartphones", "Eletrônicos"]
				    }
				""";

		mvc.perform(post("/supermarket/product/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productJson)
				.with(csrf()))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value("Smartphone"))
				.andExpect(jsonPath("$.brand").value("LG"));
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testGetStockWithAuth() throws Exception {
		mvc.perform(get("/supermarket/stock/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.quantity").value(20));
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testEntryStockWithAuth() throws Exception {
		String stockJson = """
				    {
				        "quantity": 5
				    }
				""";

		mvc.perform(put("/supermarket/stock/2/entries")
				.contentType(MediaType.APPLICATION_JSON)
				.content(stockJson)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.quantity").value(37));
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testExitStockWithAuth() throws Exception {
		String stockJson = """
				    {
				        "quantity": 5
				    }
				""";

		mvc.perform(put("/supermarket/stock/2/exits")
				.contentType(MediaType.APPLICATION_JSON)
				.content(stockJson)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.quantity").value(27));
	}


	@Test
	@WithUserDetails("admin@gmail.com")
	public void testUpdateProductWithAuth() throws Exception {
		String productJson = """
				    {
				        "name": "Smartphone",
				        "brand": "LG",
				        "price": 3000,
				        "description": "O melhor da Samsung"
				    }
				""";

		mvc.perform(put("/supermarket/product/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productJson)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value("Smartphone"))
				.andExpect(jsonPath("$.brand").value("LG"));
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testDeleteProductWithAuth() throws Exception {
		mvc.perform(delete("/supermarket/product/4")
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("admin@gmail.com")
	public void testCreateCategory() throws Exception {
		String jsonSend = """
				{
				                "name": "Ferramentas"
				            }
				""";
		String jsonExpect = """
				{
				    "id": 5,
				                "name": "Ferramentas"
				            }
				""";
		mvc.perform(post("/supermarket/category/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonSend)
				.with(csrf()))
				.andExpect(status().isCreated())
				.andExpect(content().json(jsonExpect));
	}

	@Test
	@WithMockUser(username = "joao@gmail.com", roles = "CLIENTE")
	public void testAddCartItem() throws Exception {
		mvc.perform(post(baseUrl + "/cart/addItem/1")
				.queryParam("quantity", "2")
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "joao@gmail.com", roles = "CLIENTE")
	public void testCreateOrder() throws Exception {
		mvc.perform(post(baseUrl + "order/create")
				.with(csrf()))
				.andExpect(status().isCreated());
	}

}
