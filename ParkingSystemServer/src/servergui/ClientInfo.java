package servergui;

public class ClientInfo {
    private String ip;
    private String host;
    private String status;
    private int id;

    public ClientInfo(String ip, String host, String status, int id) {
        this.ip = ip;
        this.host = host;
        this.status = status;
        this.id = id;
    }

    public String getIp() { return ip; }
    public String getHost() { return host; }
    public String getStatus() { return status; }
    public int getId() { return id; }

    public void setStatus(String status) { this.status = status; }
}
