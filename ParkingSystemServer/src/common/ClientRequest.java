package common;

import java.io.Serializable;

public class ClientRequest implements Serializable {
    private String command;
    private Object[] params;

    public ClientRequest(String command, Object[] params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public Object[] getParams() {
        return params;
    }
}
