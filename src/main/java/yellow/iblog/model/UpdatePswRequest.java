package yellow.iblog.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePswRequest {
    private Long uid;
    private String oldPsw;
    private String newPsw;
}
