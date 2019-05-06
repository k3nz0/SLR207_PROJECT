echo "Copy SLAVE.jar"
cp ~/SLAVE.jar /tmp/mezzeddine/
echo "Copy MASTER.jar"
cp ~/MASTER.jar /tmp/mezzeddine/
chmod +x /tmp/mezzeddine/SLAVE.jar
chmod +x /tmp/mezzeddine/MASTER.jar
echo "Copy splits"
cp  -r ~/splits/ /tmp/mezzeddine/
echo "Copy machines"
cp ~/machines /tmp/mezzeddine/
