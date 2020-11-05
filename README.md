# Shopping_List_Application

This application is using SQLite database to store items.
The table contains feilds like name, descripton, category and quantity.
Custom content provider is used to implement the dabatase commands like update, delete, query..
All databse constants are defiend in contarct file.
UI and databse are seperated from each other and the data will be loaded to UI using custom adapter.
Custom adapter extends cusrsor adapter to get the cursor object and load the data to UI from database.
To notify the changes of add new item, update and delete, I have implemented notification listeners.
Main activity i.e CatalogActivity and EditorActivity both implements LoaderCallback<Cursor> interface which perform query from database and returns cursor object in OnLoadFinised method.

Whole application is written in Kotlin.

