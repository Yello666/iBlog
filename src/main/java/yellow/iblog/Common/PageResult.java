package yellow.iblog.Common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Integer total;//总记录数
    private List<T> rows;//查询结果
}
