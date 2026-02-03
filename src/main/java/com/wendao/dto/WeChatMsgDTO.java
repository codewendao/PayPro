package com.wendao.dto;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class WeChatMsgDTO {
    private long seq;
    private long id;
    private Date time;
    private String talker;
    private String talkerName;
    private boolean isChatRoom;
    private String sender;
    private String senderName;
    private boolean isSelf;
    private int type;
    private int subType;
    private String content;
    private MsgContentsDTO contents;
}
