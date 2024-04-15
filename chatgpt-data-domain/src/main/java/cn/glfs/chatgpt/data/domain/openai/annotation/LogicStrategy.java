package cn.glfs.chatgpt.data.domain.openai.annotation;


import cn.glfs.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解类
 */
@Target({ElementType.TYPE})//@Target({ElementType.TYPE}) 表示定义的注解 @LogicStrategy 只能用于类、接口或枚举等类型声明上。
@Retention(RetentionPolicy.RUNTIME)//表示定义的注解 @LogicStrategy 在运行时可以通过反射等方式得到保留，并且可以被读取。
public @interface LogicStrategy {

    DefaultLogicFactory.LogicModel logicMode();

}
