package leo.tools.not_organized;

/**
 * Created by sh00514 on 2017/8/10.
 * exception utils
 */
public class ExcepUtil {
    public static RuntimeException valueIsNull(){
        return new RuntimeException("value is null");
    }

    public static RuntimeException noRecordFound(){
        return new RuntimeException("no record found");
    }
}
