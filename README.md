# QCloudImageUploaderForMarkDown
Use this to help you upload images to QCloud and replace images urls in you markdown files.

# Usage
    
    ./gradlew :assembleDist

explode `build/distributions/QCloudImageUploader-1.0-SNAPSHOT.zip`，complete `config/settings.properties`，and run with

    bin/QCloudImageUploader
    
APP_ID/APP_SECRET_ID/APP_SECRET_KEY/BUCKET can also be configured in command line.

            -appId   	          	          	  appId.              
            -bucket  	          	          	  bucketName          
            -c       	[config]  	          	  Config File contains APP_ID/APP_SECRET_ID/APP_SECRET_KEY/BUCKET.
            -f       	[file]    	          	      File or Directory to upload. Default for current directory.
            -h       	[help]    	          	  Print usages.       
            -r       	[remove] 	          	  Remove image files after uploading.
            -m       	[mdfile]  	          	  Markdown File or Directory to update. Default for current directory.
            -region  	          	          	  region, eg. tj      
            -secretId	          	          	  secretId            
            -secretKey	          	          	  secretKey     
            
            
eg.
    
    bin/QCloudImageUploader -i -f testfiles/images -m testfiles/posts
    
# Feedback

This project is still on going. Feel free to create issues.