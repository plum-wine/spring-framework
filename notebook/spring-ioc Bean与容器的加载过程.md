#### 启动代码
```
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfiguration.class);
Login login = applicationContext.getBean("login", Login.class);
```

#### ApplicationContext构造函数
```
public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
    this();
    // 这里不关心是如何通过注解来注册, 重点放在ioc如何加载Bean
    register(componentClasses);
    // refresh就是ioc初始化的地方
    refresh();
}
```
refresh的实现在AnnotationConfigApplicationContext的父类的父类, 也就是在AbstractApplicationContext中

#### AbstractApplicationContext
##### AbstractApplicationContext#refresh()
```
@Override
public void refresh() throws BeansException, IllegalStateException {
    // 加载的时候上锁
    synchronized (this.startupShutdownMonitor) {
        prepareRefresh();
        // 生成BeanDefinition, 并进行注册
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
        // 注册系统级别的Bean, 比如ApplicationContextAwareProcessor等
        prepareBeanFactory(beanFactory);
        try {
            // 允许子类去注册PostProcessor
            // 这个方法在AbstractApplicationContext中是一个抽象方法, 由子类去实现
            postProcessBeanFactory(beanFactory);

            // 调用容器级别的BeanFactoryPostProcessor中的方法
            invokeBeanFactoryPostProcessors(beanFactory);

            // 注册Bean级别的PostProcessor
            registerBeanPostProcessors(beanFactory);

            ...

            // 在这里完成工厂初始化以及所有剩余的单例Bean的初始化
            finishBeanFactoryInitialization(beanFactory);
            finishRefresh();
        }
        catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                        "cancelling refresh attempt: " + ex);
            }
            destroyBeans();
            cancelRefresh(ex);
            throw ex;
        }
        finally {
            resetCommonCaches();
        }
    }
}
```
##### AbstractApplicationContext#finishBeanFactoryInitialization()
```
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        ...
		// 实例化剩余的非延迟加载的Bean 默认实现在DefaultListableBeanFactory
		beanFactory.preInstantiateSingletons();
	}
```

#### DefaultListableBeanFactory#preInstantiateSingletons()
```
public void preInstantiateSingletons() throws BeansException {
    // 获取BeanDefinition, 这个在之前以及加载了, 本篇不分析BeanDefinition是如何加载的
    List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

    // 开始加载所有非懒加载的单例Bean
    for (String beanName : beanNames) {
        RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
        // 可以被实例化 && 单例 && 非懒加载
        if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
            // 判断是不是FactoryBean
            if (isFactoryBean(beanName)) {
                // 获取到FactoryBean本身
                Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                if (bean instanceof FactoryBean) {
                    final FactoryBean<?> factory = (FactoryBean<?>) bean;
                    boolean isEagerInit;
                    if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                        isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                        ((SmartFactoryBean<?>) factory)::isEagerInit,
                                getAccessControlContext());
                    }
                    else {
                        isEagerInit = (factory instanceof SmartFactoryBean &&
                                ((SmartFactoryBean<?>) factory).isEagerInit());
                    }
                    if (isEagerInit) {
                        getBean(beanName);
                    }
                }
            } else {
                // 这里才是加载用户定义的Bean的地方
                // 具体的实现在DefaultListableBeanFactory的父类的父类, 也就是AbstractBeanFactory中
                getBean(beanName);
            }
        }
    }
    ...
}
```

#### AbstractBeanFactory
##### AbstractBeanFactory#getBean
```
public Object getBean(String name) throws BeansException {
    return doGetBean(name, null, null, false);
}
```
##### AbstractBeanFactory#doGetBean
```
	protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
			@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
        // 解析Bean的名称, 这其中包括了解析出FactoryBean的真实名称, 如果是普通Bean, 不会做任何处理
		final String beanName = transformedBeanName(name);
		Object bean;

		// 第一次getSingleton
		// 尝试从缓存中获取bean实例
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			if (logger.isTraceEnabled()) {
				// 如果正在创建中, 与循环依赖有关
				if (isSingletonCurrentlyInCreation(beanName)) {
					logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				} else {
					logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
			// 普通Bean直接返回
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}

		else {
			// spring对于prototype类型的bean的循环依赖, 无解, 在这里直接抛出异常
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// 如果父容器存在, 尝试从父容器中获取, 父子容器的场景在spring-mvc中出现过, web容器与root容器
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (parentBeanFactory instanceof AbstractBeanFactory) {
					return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
							nameToLookup, requiredType, args, typeCheckOnly);
				}
				else if (args != null) {
					// Delegation to parent with explicit args.
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else if (requiredType != null) {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
				else {
					return (T) parentBeanFactory.getBean(nameToLookup);
				}
			}

            // typeCheckOnly的含义是, 不是类型加载, 而是创建Bean
			if (!typeCheckOnly) {
				// 标记为已创建, 加入到一个泛型为String的Set集合中
				markBeanAsCreated(beanName);
			}

			try {
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// 处理dependOn关系
				// Guarantee initialization of beans that the current bean depends on.
				String[] dependsOn = mbd.getDependsOn();
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						// 出现 A dependOn B 且 B dependOn A的时候, 直接抛出异常, spring不支持此种处理
						if (isDependent(beanName, dep)) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
						}
						registerDependentBean(dep, beanName);
						try {
							// 加载dependOn的bean
							getBean(dep);
						}
						catch (NoSuchBeanDefinitionException ex) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
						}
					}
				}

				// Create bean instance.
				if (mbd.isSingleton()) {
					// 开始创建单例的bean实例, 第二个参数是ObjectFactory
					// 创建之后数据就出现在了第一级缓存中(getSingleton执行完)
					sharedInstance = getSingleton(beanName, () -> {
						try {
							// 这一步中有Bean的自动装配, 被子类也就是AbstractAutowireCapableBeanFactory实现
							return createBean(beanName, mbd, args);
						} catch (BeansException ex) {
							// Explicitly remove instance from singleton cache: It might have been put there
							// eagerly by the creation process, to allow for circular reference resolution.
							// Also remove any beans that received a temporary reference to the bean.
							destroySingleton(beanName);
							throw ex;
						}
					});
					// 直接返回bean实例或者返回FactoryBean创建的实例
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}

				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}

                // 根据scope进行加载, 比如request, session, application等, 此处不进行分析
				else {
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, () -> {
							beforePrototypeCreation(beanName);
							try {
								return createBean(beanName, mbd, args);
							}
							finally {
								afterPrototypeCreation(beanName);
							}
						});
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new BeanCreationException(beanName,
								"Scope '" + scopeName + "' is not active for the current thread; consider " +
								"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
								ex);
					}
				}
			}
			catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}

        // 根据传入的值进行类型检查
		// Check if required type matches the type of the actual bean instance.
		if (requiredType != null && !requiredType.isInstance(bean)) {
			try {
				T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
				if (convertedBean == null) {
					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
				}
				return convertedBean;
			}
			catch (TypeMismatchException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}
```

#### AbstractAutowireCapableBeanFactory
##### AbstractAutowireCapableBeanFactory#createBean()
```
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
        throws BeanCreationException {

    if (logger.isTraceEnabled()) {
        logger.trace("Creating instance of bean '" + beanName + "'");
    }
    RootBeanDefinition mbdToUse = mbd;

    // Bean类型解析
    // Make sure bean class is actually resolved at this point, and
    // clone the bean definition in case of a dynamically resolved Class
    // which cannot be stored in the shared merged bean definition.
    Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
    if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
        mbdToUse = new RootBeanDefinition(mbd);
        mbdToUse.setBeanClass(resolvedClass);
    }

    // Prepare method overrides.
    try {
        mbdToUse.prepareMethodOverrides();
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
                beanName, "Validation of method overrides failed", ex);
    }

    try {
        // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
        // BeanPostProcessors可以自己定义返回值, 这一步如果BeanFactory发现了返回值, 代表用户自定义了proxy, BeanFactory不再进行后续处理
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
        // 此处判断代表, 如果创建出了Bean, 表示该Bean是被用户接管的, spring不再进行处理
        if (bean != null) {
            return bean;
        }
    }
    catch (Throwable ex) {
        throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                "BeanPostProcessor before instantiation of bean failed", ex);
    }

    try {
        // 创建Bean
        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        if (logger.isTraceEnabled()) {
            logger.trace("Finished creating instance of bean '" + beanName + "'");
        }
        return beanInstance;
    }
    catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
        // A previously detected exception with proper bean creation context already,
        // or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
        throw ex;
    }
    catch (Throwable ex) {
        throw new BeanCreationException(
                mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
    }
}
```
##### AbstractAutowireCapableBeanFactory#doCreateBean()
```
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
        throws BeanCreationException {

    // Instantiate the bean.
    BeanWrapper instanceWrapper = null;
    if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    }
    if (instanceWrapper == null) {
        // 1. 工厂方法创建
        // 2. 构造方法
        // 3. 无参构造器
        instanceWrapper = createBeanInstance(beanName, mbd, args);
    }
    final Object bean = instanceWrapper.getWrappedInstance();
    Class<?> beanType = instanceWrapper.getWrappedClass();
    if (beanType != NullBean.class) {
        mbd.resolvedTargetType = beanType;
    }

    // Allow post-processors to modify the merged bean definition.
    synchronized (mbd.postProcessingLock) {
        if (!mbd.postProcessed) {
            try {
                // Autowired的属性在这里获取
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Post-processing of merged bean definition failed", ex);
            }
            mbd.postProcessed = true;
        }
    }

    // 允许提前暴露的三个条件
    // 1. 单例
    // 2. 工厂允许循环引用
    // 3. 该bean在创建中

    // Eagerly cache singletons to be able to resolve circular references
    // even when triggered by lifecycle interfaces like BeanFactoryAware.
    boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
            isSingletonCurrentlyInCreation(beanName));
    if (earlySingletonExposure) {
        if (logger.isTraceEnabled()) {
            logger.trace("Eagerly caching bean '" + beanName +
                    "' to allow for resolving potential circular references");
        }
        // 如果当前Bean没有被创建, 则加入到第三级缓存中, 且删除掉二级缓存中的实例
        // 且此处可能会有aop处理
        // 这个地方并不会立即调用getEarlyBeanReference方法, 只是把ObjectFactory加入到第三级缓存中
        // 具体的调用在下一次getSingleton中
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
    }

    // Initialize the bean instance.
    Object exposedObject = bean;
    try {
        // 属性装配之前，bean的实例已经被加入到了第三级缓存中
        // 如果后续存在循环依赖, 在getSingleton时会被创建到第二级缓存中， 然后提前暴露
        // 属性装配

        // processor
        // 1. ApplicationContextAwareProcessor InjectionMetadata AutowiredFieldElement

        populateBean(beanName, mbd, instanceWrapper);
        // 1. 判断是否实现了BeanNameAware或者BeanClassLoaderAware,BeanFactoryAware. 如果有则设置相关属性
        // 2. 调用Bean初始化的前置操作(BeanPostProcessor)
        // 3. 执行初始化方法
        //    1. 如果实现了InitializingBean, 则调用afterPropertiesSet
        //    2. 如果有initMethod方法, 则调用
        // 4. 调用Bean初始化的后置操作(BeanPostProcessor)
        exposedObject = initializeBean(beanName, exposedObject, mbd);
    }
    catch (Throwable ex) {
        if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
            throw (BeanCreationException) ex;
        }
        else {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
        }
    }

    if (earlySingletonExposure) {
        // 实例进入二级缓存
        Object earlySingletonReference = getSingleton(beanName, false);
        if (earlySingletonReference != null) {
            if (exposedObject == bean) {
                exposedObject = earlySingletonReference;
            }
            else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                String[] dependentBeans = getDependentBeans(beanName);
                Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                for (String dependentBean : dependentBeans) {
                    if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                        actualDependentBeans.add(dependentBean);
                    }
                }
                if (!actualDependentBeans.isEmpty()) {
                    throw new BeanCurrentlyInCreationException(beanName,
                            "Bean with name '" + beanName + "' has been injected into other beans [" +
                            StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                            "] in its raw version as part of a circular reference, but has eventually been " +
                            "wrapped. This means that said other beans do not use the final version of the " +
                            "bean. This is often the result of over-eager type matching - consider using " +
                            "'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
                }
            }
        }
    }

    // Register bean as disposable.
    try {
        registerDisposableBeanIfNecessary(beanName, bean, mbd);
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanCreationException(
                mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
    }

    return exposedObject;
}
```