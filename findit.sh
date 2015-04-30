for i in $(cat clspathfile); do
    #statements
    jar ft $i | grep -q org.jboss.aerogear.unifiedpush.message.Message
    if [[ $? -eq 0 ]]; then
        #statements
        echo $i
        jar ft $i | grep org.jboss.aerogear.unifiedpush.message.Message
    fi
done