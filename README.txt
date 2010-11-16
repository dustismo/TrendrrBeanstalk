A Java beanstalk client


I wrote this client because we needed a pooled high performance client to interact with beanstalk.  
It uses socket channels instead of regular sockets for increased throughput (in our environment it is 10 to 20X faster then the regular socket implementation). 

There is a simple connection pool so client connections can be reused.

See src/Example for example usage.

License is LGPL