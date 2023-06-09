# Staff api service

## Entities:

1. User:
    * Свойства:
        * Список групп пользователя;
        * Список контактов пользователя;
        * Список провайдеров пользователя;
        * Список свойств пользователя;
        * Список статусов пользователя;
    * Доступы:
        * Доступ к методу эндпоинта пользователя;
        * Доступ к объекту пользователя;
        * Доступ к пользователям по группе;
        * Доступ к пользователям по статусу;
2. Group:
    * Свойства:
        * Родительская группа группы;
        * Список свойств группы пользователя;
        * Список статусов группы пользователя;
    * Доступы:
        * Доступ к методу эндпоинта группы;
        * Доступ к объекту группы;
        * Доступ к группе по родительской группе;
3. Contact:
    * Свойства:
        * Тип контакта;
        * Список свойств контакта пользователя;
        * Список статусов контакта пользователя;
    * Доступы:
        * Доступ к методу эндпоинта контакта;
4. Provider:
    * Свойства:
        * Список свойств провайдера авторизации;
        * Список статусов провайдера авторизации;
    * Доступы:
        * Доступ к методу эндпоинта провайдера;
5. Property:
    * Свойства:
        * Список свойств свойства;
        * Список статусов свойства;
    * Доступы:
        * Доступ к методу эндпоинта свойства;
        * Доступ к значениям свойства;
6. Status:
    * Свойства:
        * Список свойств статуса;
        * Список статусов статуса;
    * Доступы:
        * Доступ к методу эндпоинта статуса;
        * Доступ к значениям статуса;
7. Directory:
    * Свойства:
        * Список свойств справочника;
        * Список статусов справочника;
    * Доступы:
        * Доступ к методу эндпоинта справочника;
8. Value:
    * Свойства:
        * Спрвочник свойства;
        * Список свойств значения;
        * Список статусов значения;
    * Доступы:
        * Доступ к методу эндпоинта значения;
        * Доступ к значениям значения;
9. Language:
10. Method Permission
    * Свойства:
        * Метод эндпоинта;
        * Объект доступа;
        * Группа пользователей;
    * Доступы
        * Доступ к методу эндпоинта доступов;
        * Доступ к объекту доступов;

## Endpoint

1. ``/user``
    * Параметры
        * Фильтр списка пользователя по группе пользователя;
        * Пагинация списка пользователей limit, offset;
2. ``/myself``
    * Параметры
3. ``/group``
    * Параметры
        * Фильтр групп по родительской группе
        * Фильтр всех групп в дереве по родительской группе
        * Пагинация списка групп limit, offset
4. ``/contact``
    * Параметры:
        * Пагинация списка контактов limit, offset;
5. ``/provider``
    * Параметры:
        * Пагинация списка провайдеров limit, offset;
6. ``/property``
    * Параметры:
        * Пагинация списка свойств limit, offset;
7. ``/status``
    * Параметры:
        * Пагинация списка статусов limit, offset;
8. ``/permission``

## Versions

* 0.1.0
    * Объект пользователя;
        * Модель пользователя;
        * Эндпоинт пользователя;
        * Миграция модели пользователя;
    * Объект группы пользователя;
        * Модель группы пользователя;
        * Эндпоинт группы пользователя;
        * Миграция модели группы пользователя;
* 0.1.1
    * Список групп пользователя;
    * Родительская группа группы;
        * Миграция свойств родительской группы групп;
    * Доступ к методу эндпоинта пользователей;
        * Миграция доступов к методу эндпоинта пользователей;
* 0.1.2
    * Дешифровка веб токена;
    * Получения объекта сессии;
    * Доступ к методу эндпоинта группы;
* 0.1.3:
    * Фильтр списка пользователя по группе пользователя;
    * Пагинация списка пользователей limit, offset;
    * Доступ к объекту пользователя;
* 0.1.4:
    * Обработка ошибок;
* 0.2.0:
    * Контакты:
        * Модель контактов пользователя;
        * Эндпоинт контакта пользователя;
        * Список контактов в объекте пользователя;
    * Доступы к методу:
        * Эндпоинт доступов к методу;
* 0.2.1
* 0.3:
    * Объект провайдеры;
    * Объект свойства;
    * Объект статуса;
* 0.3.0:
    * Объект провайдера:
        * Модель провайдера;
        * Контроллер провайдера;
    * Значение провайдера у пользователя;
* 0.3.1:
    * Объект свойства:
        * Модель свойства;
        * Контроллер свойства;
* 0.3.2:
    * Объект статуса:
        * Модель статуса;
        * Контроллер статуса;
* 0.4:
    * Объект справочника;
        * Модель справочника;
        * Контроллер справочника;
    * Объект значения справочника;
        * Модель значения справочника;
        * Контроллер значения справочника;
* 0.5:
    * Объект языка;
        * Модель языка;
        * Контроллер языка;
* 0.6:
    * Свойства пользователя
    * Статусы пользователя
* 0.7:
    * Свойства группы, контакта, провайдера, свойства, статуса, языка;
    * Статусы группы, контакта, провайдера, свойства, статуса, языка;
