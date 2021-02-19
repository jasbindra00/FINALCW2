public interface PacketHandler
{
    void handlePacket(Packet packet, PacketReceiver server_listener);
}
