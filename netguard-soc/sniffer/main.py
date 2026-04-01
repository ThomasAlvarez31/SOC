from scapy.all import sniff, IP, TCP, UDP
import requests

API_URL = "http://localhost:8080/api/packet"

def process_packet(pkt):
    if IP in pkt:
        # Identificar protocolo
        proto = "TCP" if TCP in pkt else "UDP" if UDP in pkt else "ICMP"
        
        payload = {
            "src": pkt[IP].src,
            "dst": pkt[IP].dst,
            "port": pkt.sport if hasattr(pkt, 'sport') else 0,
            "proto": proto,
            "size": len(pkt)
        }
        
        try:
            requests.post(API_URL, json=payload, timeout=0.05)
        except:
            pass

print("📡 NETGUARD CORE: SISTEMA DE VIGILANCIA ACTIVO...")
sniff(prn=process_packet, store=0)