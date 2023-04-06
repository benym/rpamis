package com.benym.rpamis.common.dto.enums;

/**
 * @author benym
 * @date 2022/7/8 4:18 下午
 */
public enum TraceType {

    /**
     * rpamis默认实现
     */
    RPAMIS("rpamis","rpamis默认实现"),
    /**
     * skywalking实现
     */
    SKYWALK("skywalking","skywalking实现");

    private String type;
    private String desc;

    TraceType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
