package com.imglog.myimagelogserver.stockreport.domain;

import com.imglog.myimagelogserver.image.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name="stock_report_subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReportSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "symbols_json", nullable = false, columnDefinition = "TEXT")
    private String symbolsJson;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name= "send_hour", nullable = false)
    private int sendHour = 14;

    public static StockReportSubscription create(Long userId, String email, String symbolsJson, int sendHour) {
        StockReportSubscription s = new StockReportSubscription();
        s.userId = userId;
        s.email = email;
        s.symbolsJson = symbolsJson;
        s.sendHour = sendHour;
        s.enabled = true;
        return s;
    }

    public void update(String email, String symbolsJson, int sendHour, boolean enabled) {
        this.email = email;
        this.symbolsJson = symbolsJson;
        this.sendHour = sendHour;
        this.enabled = enabled;
    }
}
