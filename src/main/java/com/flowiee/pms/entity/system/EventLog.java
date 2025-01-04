package com.flowiee.pms.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "event_logs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long requestId;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "process_class")
    private String processClass;

    @Column(name = "process_method")
    private String processMethod;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "request_param", length = 999)
    private String requestParam;

    @Column(name = "request_body", length = 4000)
    private String requestBody;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "application")
    private String application;

    @Column(name = "entity")
    private String entity;
}