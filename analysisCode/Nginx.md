
# Nginx

- Nginx를 설치한 후에 `/etc/nginx/sites-available`로 이동합니다.
- `sudo vi default`를 통해 default 파일을 수정합니다.

>

    server {
      	root /var/www/html;
      	...
      	location / {
	      	//root 에 실패 시 try_files가 호출되며 404 에러 메세지를 띄웁니다.
      		try_files $uri $uri/=404; 
      	}
      	...
      }

> 아래와 같이 수정합니다.

    server {
    	...
    	location / {
	    	proxy_pass http://168.188.127.192:3000;
    	}
    	location /board {
	    	proxy_pass http://168.188.127.192:3000/board;
    	}
    	location /ack {
	    	proxy_pass http://168.188.127.192:3000/ack;
    	}
    	...
    }
	
- `sudo service nginx restart` 를 통하여 Nginx를 다시 시작합니다.

  > default(80) 포트로 접속 시, 3000 포트로 포트 포워딩된 결과를 확인할 수 있습니다.
