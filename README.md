Менеджер учетных записей для службы каталогов LDAP
==================================================

JAVA приложение для администрирования учетных службы каталогов, разработанное студентами Амурского государственного университета. При разработке использовалась [NetBeans IDE 7.2.1 for Java].

Установка
---------

### Установка проекта

Скачайте архив [AccountManager], распакуйте его. Далее под %Папка проекта% понимается полный URL вместе с директорией AcountManager-master до распакованных файлов.

### Зависимости  

Для корректной работы системы необходимо устранить некоторые зависимости:  
1. [Novell LDAP classes for Java]. Скачайте novell-jldap-devel-.zip и извлеките архив. Скопируйте файлы jaxp.jar, jmxri.jar, crimson.jar из %Папка в которую извлечен архив%/lib/  в %Папка проекта%/requered/.   
2. [JConfig] используется для организации хранения настроек программы. Скачайте jconfig-bin-.zip и извлеките архив. Скопируйте файлы jaxp.jar, jmxri.jar, crimson.jar из %Папка в которую извлечен архив%/lib/  в %Папка проекта%/requered/.  
3. [Open LDAP] - Open LDAP Server.  

### Установка Open LDAP  
    sudo apt-get install slapd ldap-utils  
    sudo dpkg-reconfigure slapd  
Далее отвечайте на вопросы:  

    Не выполнять настройку сервера OpenLDAP? - Нет  
Ответы на следующие вопросы будут уникальны для конкретной конфигурации, кроме последнего:  

    Использовать протокол LDAP v2? - Нет  

## Настройка программы  

### Минимальная конфигурация базы данных Open LDAP

После установки OpenLDAP создайте файл mybase.ldif: 
 
    dn: cn=Groups,dc=yourdomain,dc=yourzone
    cn: Groups
    objectClass: organizationalRole
    objectClass: top  

    dn: cn=Employees,dc=yourdomain,dc=yourzone
    cn: Employees
    objectClass: organizationalRole
    objectClass: top  

    dn: cn=Departments,dc=yourdomain,dc=yourzone
    cn: Departments
    objectClass: organizationalRole
    objectClass: top  

    dn: uid=johnsmith,cn=Employees,dc=yourdomain,dc=yourzone
    uid: johnsmith
    cn: johnsmith
    sn: Smith
    givenName: John
    telephoneNumber: 1-234-56
    roomNumber: 222
    title: Postman
    ou: Post Unit
    objectClass: inetOrgPerson
    displayName: John Smith 
    mail: john@mail.ru

    dn: cn=Postmans,cn=Groups,dc=yourdomain,dc=yourzone
    objectClass: groupOfNames
    cn: Postmans
    description: Почтальоны
    member: uid=johnsmith,cn=Employees,dc=yourdomain,dc=yourzone

    dn: ou=Post,cn=Departments,dc=yourdomain,dc=yourzone
    ou: Post Unit
    description: Почта 
    objectClass: organizationalUnit

Для ввода данных в базу данных выполните:

    ldapadd -x -D cn=admin,dc=yourdomain,dc=yourzone -W -b dc=yourdomain,dc=yourzone\
    -f "mybase.ldif"

или импортируйте созданный файл с помощью любого LDAP-браузера.

### Настройка приложения

Откройте файл %Папка проекта%/accountmanager.xml и исправьте данные следующим образом:  

1. В  секции *server* в свойство *host* установите адрес OpenLDAP сервера;  
2. В  секции *server* в свойство *user_base* установите адрес директории поиска пользователей в случае примера это будет cn=Employees,dc=yourdomain,  dc=yourzone;  
3. В  секции *server* в свойство *group_base* установите адрес директории поиска групп;  
4. В  секции *server* в свойство *bind_dn* установите dn пользователя под которым будет производится соединение.  Примечание: для внесения изменения в базу данных необходимо, чтобы данный пользователь имел права администратора.

На этом все настройки завершены. Теперь нужно открыть проект NetBeans и запустить приложение. Приятного использования.
 
[AccountManager]: https://github.com/AmurSU/AccountManager/archive/master.zip "AccountManager sources"
[NetBeans IDE 7.2.1 for Java]: http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html "NetBeans for Java"
[Novell LDAP classes for Java]: http://www.novell.com/developer/ndk/ldap_classes_for_java.html "Novell LDAP classes"
[JConfig]: http://www.jconfig.org/TheDownloads.html "JConfig classes"
[Open LDAP]: hhttp://www.openldap.org/software/download/ "Open LDAP download"
