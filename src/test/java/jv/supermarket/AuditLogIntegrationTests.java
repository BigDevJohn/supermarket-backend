package jv.supermarket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jv.supermarket.audit.AuditLog;
import jv.supermarket.audit.AuditLogRepository;
import jv.supermarket.audit.enums.Action;
import jv.supermarket.audit.enums.EntityType;
import jv.supermarket.order.Order;
import jv.supermarket.order.OrderRepository;
import jv.supermarket.order.enums.OrderStatus;
import jv.supermarket.user.User;
import jv.supermarket.user.UserRepository;

@SpringBootTest(classes = SupermarketApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditLogIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @WithUserDetails("admin@supermarket.local")
    void stockExitCreatesStockUpdateAudit() throws Exception {
        mvc.perform(put("/supermarket/stock/2/exits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": 5}
                        """)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.quantity")
                        .value(27));

        AuditLog audit = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType.STOCK, 2L, PageRequest.of(0, 1))
                .getContent().get(0);
        assertEquals(Action.UPDATE, audit.getAction());
        assertEquals(EntityType.STOCK, audit.getEntityType());
        assertEquals(2L, audit.getEntityId());
        assertTrue(audit.getDetails().contains("from 32 to 27"));
    }

    @Test
    @WithUserDetails("customer@supermarket.local")
    void cancellingCustomerOrderCreatesCancellationAuditAndUpdatesStatus() throws Exception {
        mvc.perform(post("/supermarket/order/create").with(csrf()))
                .andExpect(status().isCreated());

        User customer = userRepository.findByEmail("customer@supermarket.local");
        Order order = orderRepository.findByUserId(customer.getId(),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))).getContent().get(0);

        mvc.perform(delete("/supermarket/order/" + order.getId() + "/cancel").with(csrf()))
                .andExpect(status().isOk());

        Order cancelled = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        AuditLog audit = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType.ORDER, order.getId(),
                        PageRequest.of(0, 1))
                .getContent().get(0);
        assertEquals(Action.UPDATE, audit.getAction());
        assertEquals(EntityType.ORDER, audit.getEntityType());
        assertEquals(order.getId(), audit.getEntityId());
        assertTrue(audit.getDetails().contains("Order cancelled"));
    }

    @Test
    @WithUserDetails("admin@supermarket.local")
    void adminCanListStockAudits() throws Exception {
        mvc.perform(put("/supermarket/stock/2/exits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": 5}
                        """)
                .with(csrf()))
                .andExpect(status().isOk());

        mvc.perform(get("/supermarket/audit/by-entity-type/STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].entityType").value("STOCK"))
                .andExpect(jsonPath("$.content[0].action").value("UPDATE"))
                .andExpect(jsonPath("$.content[0].entityId").value(2))
                .andExpect(jsonPath("$.content[0].details").value(org.hamcrest.Matchers.containsString("from 32 to 27")));
    }

    @Test
    @WithUserDetails("employee@supermarket.local")
    void employeeCanListStockAudits() throws Exception {
        mvc.perform(put("/supermarket/stock/2/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": 5}
                        """)
                .with(csrf()))
                .andExpect(status().isOk());

        mvc.perform(get("/supermarket/audit/by-entity-type/STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].entityType").value("STOCK"))
                .andExpect(jsonPath("$.content[0].action").value("UPDATE"))
                .andExpect(jsonPath("$.content[0].entityId").value(2))
                .andExpect(jsonPath("$.content[0].details").value(org.hamcrest.Matchers.containsString("from 32 to 37")));
    }

    @Test
    @WithUserDetails("customer@supermarket.local")
    void customerCannotListStockAudits() throws Exception {
        mvc.perform(get("/supermarket/audit/by-entity-type/STOCK"))
                .andExpect(status().isForbidden());
    }
}
