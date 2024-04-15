package cn.glfs.chatgpt.data.domain.openai.service.rule.factory;


import cn.glfs.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.glfs.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import com.alibaba.fastjson2.util.AnnotationUtils;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则工厂
 */
@Service
public class DefaultLogicFactory {
    public Map<String, ILogicFilter> logicFilterMap = new ConcurrentHashMap<>();

    //在构造函数中遍历传入的逻辑过滤器列表，对于每个逻辑过滤器，通过查找其类上的 LogicStrategy 注解，如果找到了注解，
    // 就将该注解中定义的逻辑模式和对应的逻辑过滤器实例一一对应地放入 logicFilterMap 中进行管理。
    public DefaultLogicFactory(List<ILogicFilter> logicFilterList){
        logicFilterList.forEach(logic->{
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(),LogicStrategy.class);
            if(null!=strategy){
                logicFilterMap.put(strategy.logicMode().getCode(), logic);
            }
        });
    }

    public Map<String,ILogicFilter> openLogicFilter(){
        return logicFilterMap;
    }

    /**
     * 规则逻辑枚举
     */
    public enum LogicModel{
        NULL("NULL", "放行不用过滤"),
        ACCESS_LIMIT("ACCESS_LIMIT", "访问次数过滤"),
        SENSITIVE_WORD("SENSITIVE_WORD", "敏感词过滤"),
        USER_QUOTA("USER_QUOTA", "用户额度过滤"),
        MODEL_TYPE("MODEL_TYPE", "模型可用范围过滤"),
        ACCOUNT_STATUS("ACCOUNT_STATUS", "账户状态过滤"),

        ;
        private String code;
        private String info;
        LogicModel(String code,String info){
            this.code = code;
            this.info = info;
        }
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
