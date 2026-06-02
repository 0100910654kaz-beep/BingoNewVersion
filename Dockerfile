FROM tomcat:9.0-jdk11-corretto

# タイムゾーンを日本に設定
ENV TZ=Asia/Tokyo

# 既存のROOTアプリケーションを削除
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# プロジェクトのファイルをTomcatのROOT配下に配置
COPY index.jsp /usr/local/tomcat/webapps/ROOT/
COPY admin.jsp /usr/local/tomcat/webapps/ROOT/

# Javaファイルをコンパイルして配置するディレクトリを作成
RUN mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/classes

# Javaファイルのコンパイルと配置（3つのJavaファイルをすべて含める）
RUN javac -classpath /usr/local/tomcat/lib/servlet-api.jar -d /usr/local/tomcat/webapps/ROOT/WEB-INF/classes BingoServlet.java BingoGame.java PlayerResult.java

# ポート番号の設定（Render用）
EXPOSE 8080
CMD ["catalina.sh", "run"]
