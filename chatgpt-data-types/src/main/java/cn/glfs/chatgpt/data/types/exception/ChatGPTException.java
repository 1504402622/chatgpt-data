package cn.glfs.chatgpt.data.types.exception;

/**
 * 异常类
 */
public class ChatGPTException extends RuntimeException{

    //异常码
    private String code;
    //异常信息
    private String message;

    public ChatGPTException(String code){
        this.code = code;
    }

    public ChatGPTException(String code, Throwable cause) {
        this.code = code;
        //用于初始化当前异常的原因（cause）。这个方法允许你为一个异常设置一个原因，表示导致当前异常抛出的根本原因
        super.initCause(cause);
    }

    public ChatGPTException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ChatGPTException(String code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        super.initCause(cause);
    }


}
