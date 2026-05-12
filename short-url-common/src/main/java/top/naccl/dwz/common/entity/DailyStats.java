package top.naccl.dwz.common.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyStats {
    private Long id;
    private String shortCode;
    private LocalDate statsDate;
    private Integer pv;
    private Integer uv;
    private Integer ipCount;
}
