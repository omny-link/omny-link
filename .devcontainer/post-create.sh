#!/bin/bash
set -e

echo "Setting up CRM development environment..."

# Setup Node.js crm-ui-next project
if [ -d "crm-ui-next" ]; then
    echo "Setting up crm-ui-next..."
    cd crm-ui-next
    npm install
    cd ..
fi

echo "✓ Development environment setup complete!"
echo ""
echo "Available commands:"
echo "  - crm-server: mvn install && cd crm-server && mvn spring-boot:run"
echo "  - crm-ui: cd crm-ui && npm install && gulp server"
echo "  - crm-ui-next: cd crm-ui-next && npm run dev"
