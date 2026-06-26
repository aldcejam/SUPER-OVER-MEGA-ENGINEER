#!/bin/bash
echo "Inicializando LocalStack S3 e SQS..."

# Criar bucket
awslocal s3api create-bucket --bucket pdf-extractions

# Criar fila SQS
awslocal sqs create-queue --queue-name pdf-extraction-events

# Configurar notificação de evento do S3 para a fila SQS
awslocal s3api put-bucket-notification-configuration \
    --bucket pdf-extractions \
    --notification-configuration '{
        "QueueConfigurations": [
            {
                "QueueArn": "arn:aws:sqs:us-east-1:000000000000:pdf-extraction-events",
                "Events": ["s3:ObjectCreated:*"]
            }
        ]
    }'

echo "LocalStack S3 e SQS configurados com sucesso!"
