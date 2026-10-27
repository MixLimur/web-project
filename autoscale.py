import urllib.request
import json
import subprocess
import time
import base64
import sys

RABBITMQ_URL = "http://rabbitmq:15672/api/queues/%2f/task.queue"
USERNAME = "guest"
PASSWORD = "guest"

THRESHOLDS = {
    0: 1,
    4: 2,
    8: 3
}

CURRENT_WORKERS = 1

def get_queue_messages():
    try:
        request = urllib.request.Request(RABBITMQ_URL)
        base64string = base64.b64encode(f"{USERNAME}:{PASSWORD}".encode("utf-8")).decode("ascii")
        request.add_header("Authorization", f"Basic {base64string}")
        
        with urllib.request.urlopen(request) as response:
            data = json.loads(response.read().decode())
            return data.get("messages", 0)
    except Exception as e:
        print(f"[-] Помилка з'єднання з RabbitMQ: {e}", flush=True) 
        return -1

def scale_workers(target_workers):
    global CURRENT_WORKERS
    if target_workers == CURRENT_WORKERS:
        return

    print(f"\n[!] Зміна навантаження! Масштабуємо worker-service: {CURRENT_WORKERS} -> {target_workers}...", flush=True)
    
    try:
        subprocess.run(
            [
                "docker", "compose",
                "-f", "docker-compose.yml",
                "-p", "web-project",
                "up", "-d",
                "--no-deps",
                "--scale", f"worker-service={target_workers}",
                "worker-service"
            ],
            check=True
        )
        CURRENT_WORKERS = target_workers
        print(f"[+] Масштабування успішне. Активних воркерів: {CURRENT_WORKERS}\n", flush=True)
    except subprocess.CalledProcessError as e:
        print(f"[-] Помилка Docker Compose: {e}", flush=True)

def determine_target_workers(messages):
    target = 1
    for threshold_msgs, workers in sorted(THRESHOLDS.items()):
        if messages >= threshold_msgs:
            target = workers
    return target

if __name__ == "__main__":
    print("Запуск автономної системи масштабування (Auto-scaler)...", flush=True)
    
    while True:
        messages_in_queue = get_queue_messages()
        
        if messages_in_queue != -1:
            target = determine_target_workers(messages_in_queue)
            sys.stdout.write(f"\r📊 Завдань у черзі: {messages_in_queue} | Поточних воркерів: {CURRENT_WORKERS}")
            sys.stdout.flush()
            
            scale_workers(target)
            
        time.sleep(5)