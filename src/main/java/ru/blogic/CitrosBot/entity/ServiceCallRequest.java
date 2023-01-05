package ru.blogic.CitrosBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Сущность запроса в техподдержку
 *
 * @author eyakimov
 */
@Entity(name = "ServiceCallRequest")
@Table(name = "service_call_request")
@Getter
@ToString
@RequiredArgsConstructor
public class ServiceCallRequest {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity fromUser;

    @Column(name = "request_text")
    private String requestText;

    public static ServiceCallRequest.ServiceCallRequestBuilder newBuilder() {
        return new ServiceCallRequest().new ServiceCallRequestBuilder();
    }

    public class ServiceCallRequestBuilder {

        private ServiceCallRequestBuilder() {
        }

        public ServiceCallRequest.ServiceCallRequestBuilder setId(Long id) {
            ServiceCallRequest.this.id = id;
            return this;
        }

        public ServiceCallRequest.ServiceCallRequestBuilder setFromUser(UserEntity fromUser) {
            ServiceCallRequest.this.fromUser = fromUser;
            return this;
        }

        public ServiceCallRequest.ServiceCallRequestBuilder setRequestText(String requestText) {
            ServiceCallRequest.this.requestText = requestText;
            return this;
        }

        public ServiceCallRequest build() {
            return ServiceCallRequest.this;
        }
    }
}
