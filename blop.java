private Object getObj(String cl) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String serviceManagerName = "android.os.ServiceManager";
        String serviceManagerNativeName = "android.os.ServiceManagerNative";
        String targetName = cl;

        Class targetClass = Class.forName(targetName);
        Class targetStubClass = targetClass.getClasses()[0];
        Class serviceManagerClass = Class.forName(serviceManagerName);
        Class serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

        Object targetObject;
        Object serviceManagerObject;

        Method getService = serviceManagerClass.getMethod("getService", String.class);

        Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                "asInterface", IBinder.class);
        tempInterfaceMethod.setAccessible(true);

        Binder tmpBinder = new Binder();
        tmpBinder.attachInterface(null, "fake");
        serviceManagerObject = tempInterfaceMethod.invoke(null,  new Object[] { tmpBinder });

        Class c = Class.forName(targetName);
        String serviceName = getApplicationContext().getSystemServiceName(c);
        IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject,
                serviceName);

        Method serviceMethod = targetStubClass.getMethod("asInterface",
                IBinder.class);
        targetObject = serviceMethod
                .invoke(null, new Object[]{retbinder});

        client.Message = "Fuzzing started for "
                + targetObject.getClass().getName() + " class.\n";
        client.sendMessage();

        return targetObject;
    }
