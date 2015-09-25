# Welcome to PenKnife
Inspired by [Dagger 2][dagger-link] and [Pocket Knife][pocket-knife-link] this is a library that helps you speed up your development cycle by creating builders, getters and injectors based upon your project requirements at the cost of initial setup removing your need for intent/bundle factories. 

How to get started.

Tag a field

    @PKBind(ResultActivity.class)
    public int myInte;



Tag a Method

     @PKBind(ResultActivity.class)
    public void injectUserHere(SerializedUser user){
        getView().showUser(user);
    }

As you can see, PKBind takes one parameter of type **Class** this will indicate the **Scope** of this bindable Class/Field/Method. Meanning that any other **@PKBind** annotated elements will be grouped under one class named **PKBuild[Your_Class]** and __PKExtract[Your_Class]__. Case in point, say we have a custom view, some Class such as a ViewModel or Presenter. You would annotate methods/fields or even the classes themselves and when you annotate with **@PKBind** set the class to point at the activity you would normally extract bundled parameters from and that's it. The annotation processor will generate the Build and Extract classes. 

Usage of PKBuild (Default settings):

    Bundle bundle = PkBuildResultActivity.builder()
                        .provideFlag(booleanView.isChecked())
                        .provideMessage(welcomeMessage.getText().toString())
                        .provideUser(new SerializedUser(nameView.getText().toString(),            
                        Integer.parseInt(ageView.getText().toString())))
                        .build();
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity.this, ResultActivity.class);
                startActivity(intent)
    
Usage  of PKExtract (Default Settings):

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PKExtractResultActivity.newInstance(getIntent().getExtras())
                        .inject(getPresenter())
                        .inject(this);
    }

As you can see, by default the default settings will cause your PKBuild to return a Bundle (Or which ever type of java container you wish to utilize). Additionally even though PKExtract[**] comes with getter methods for each bundled item, it also generates an injection method for each class which featured a __@PKBind__ annotation on or in it.

## Setup instructions
* Setup gradle dependencies (TODO)
* Either utilize the [PenKnifeHandlerImpl][penknife-handler-impl] I created or make your own by implementing this interface [PenKnifeHandler][penknife-handler] This will be call back that the generated code utilizes at run time to deal with bundling/unbundling.
* Annotate any class in your project, but I recommend going with the Application file. 

        //container = class of desired java container, handlerImpl points to your IMPL of PenKnifeHandler
        @PKHandler(container = Bundle.class, handlerImpl = PenKnifeHandlerImpl.class)

* Add this line of code to your application's OnCreate (Could be any PenKnifeHandler implementation):

        PenKnife.initialize(new PenKnifeHandlerImpl());
    
* Done. Now your project is ready to be annotated!

[A gist of my DemoApplication.java](https://gist.github.com/patrykpoborca/fd1ec2b75ae5243b5ee3)

## Optional settings
When you annotate a field/method/class with **@PKBuild** you have the ability to assign a priority to that annotation. 0 is the default value, this priority is used to determine the order in which the annotated elements are injected. This is in case you have injectable elements which rely on one another to not error out.

    @PKBind(value = ResultActivity.class, priorityOfTarget = 1)
        public void injectMessageAndBoolean(boolean flag, String message){
            getView().showWelcomeMessageAndFlag(message, flag);
        }
        
This will result in this generated injectionmethod:

    void inject(ResultViewPresenter injectableField) {
        injectableField.injectMessageAndBoolean(getFlag(), getMessage());
        injectableField.injectUserHere(getUser());
      }
      

If you annotate a class with **@PKBind** you will generate provide methods/extract and (optionally) injection methods for any non privae/protected fields. Meaning in cases with several injectable fields but a few public non-injectable fields you may want to annoate the class itself to save clutter. That's where **@PKIgnore** comes in. If you annotate a field with this, it will be ignored in in the class level search.

Finally, you also have the ability to annotate a scoped class to provide settings generated code for that scope. Case in point throughout this ReadMe I have been using the **ResultActivity.class** scope from the demo. This means all I have to do is annotate the ResultActivity in order to propagate settings to its generated **PKBuildResultActivity** and **PKExtractResultActivity**.

    @PenKnifeTargetSettings(translateToClass =  Intent.class, createInjectionMethod = true)


By default the generate code will **not** use a translation class and **will** create injection methods. What this means for you, even if you annotate an activity you will get a bundle returned to you, in order to save code you simply annotate type of class you wish have the **PKBuild** return. However this mapping between the bundle and the desired return type is completely up to you. That is the reason for the map method in the PenKnifeHandler class. Here is a snipped from my implementation:

    @Override
        public Object map(Bundle container, Class<?> annotatedObject) {
            if (Intent.class.equals(annotatedObject)) {
                Intent intent = new Intent();
                intent.putExtras(container);
                return intent;
            }
            return null; //not implemented
        }

The idea here is to save you these lines of code if you annotate Activities:

    Intent intent = new Intent();
    intent.putExtras(bundle);
    

Let me know your thoughts/opinions about this library at my gmail: **poborcapatryk**
or at my [Twitter](https://twitter.com/patrykpoborca) and on if you're doing all that give me a follow on [Medium](https://medium.com/@patrykpoborca/)

### Thanks!

[dagger-link]: http://google.github.io/dagger/
[pocket-knife-link]: https://github.com/hansenji/pocketknife
[penknife-handler-impl]: https://github.com/patrykpoborca/PenKnife/blob/master/penknife-handler/src/main/java/io/patryk/PenKnifeHandlerImpl.java
[penknife-handler]:https://github.com/patrykpoborca/PenKnife/blob/master/penknifeinterfaces/src/main/java/io/patryk/PenKnifeHandler.java
