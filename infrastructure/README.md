#!/bin/bash

# Guardar como: infrastructure/ec2-setup.sh

echo "Iniciando configuración del servidor EC2..."

# Update system
sudo yum update -y

# Install Docker
echo "Instalando Docker..."
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user

# Install Docker Compose
echo "Instalando Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install AWS CLI
echo "Instalando AWS CLI..."
sudo yum install -y aws-cli

# Create application directory structure
echo "Creando estructura de directorios..."
mkdir -p ~/tourism-app/logs
cd ~/tourism-app

echo "Configuración completada!"
echo "IMPORTANTE: Necesitarás cerrar sesión y volver a conectarte para que los cambios del grupo docker surtan efecto"