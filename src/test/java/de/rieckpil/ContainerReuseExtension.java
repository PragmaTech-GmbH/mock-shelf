package de.rieckpil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * JUnit Jupiter extension that automatically applies withReuse(true) to all Testcontainers
 * containers in the test class using reflection.
 *
 * <p>Usage: @ExtendWith(ContainerReuseExtension.class)
 */
public class ContainerReuseExtension implements BeforeAllCallback, BeforeEachCallback {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerReuseExtension.class);

  @Override
  public void beforeAll(ExtensionContext context) {
    // Handle static container fields
    context
        .getTestClass()
        .ifPresent(
            testClass -> {
              applyReuseToStaticContainerFields(testClass);
            });
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    // Handle instance container fields
    context
        .getTestInstance()
        .ifPresent(
            testInstance -> {
              applyReuseToInstanceContainerFields(testInstance);
            });
  }

  private void applyReuseToStaticContainerFields(Class<?> testClass) {
    // Get all fields from the class and its superclasses
    List<Field> fields = getAllFields(testClass);

    for (Field field : fields) {
      try {
        // Only process static fields
        if (!Modifier.isStatic(field.getModifiers())) {
          continue;
        }

        // Check if field is accessible
        boolean wasAccessible = field.canAccess(null);
        if (!wasAccessible) {
          field.setAccessible(true);
        }

        // Get the field value
        Object fieldValue = field.get(null);

        // Skip if not a container or if it's null
        if (fieldValue == null) {
          continue;
        }

        // Check if it's a GenericContainer or a subclass
        if (GenericContainer.class.isAssignableFrom(field.getType())) {
          applyReuseToContainer((GenericContainer<?>) fieldValue, field.getName());
        }

        // Restore accessibility
        if (!wasAccessible) {
          field.setAccessible(false);
        }
      } catch (Exception e) {
        LOG.warn("Failed to apply reuse to static field: " + field.getName(), e);
      }
    }
  }

  private void applyReuseToInstanceContainerFields(Object testInstance) {
    // Get all fields from the class and its superclasses
    List<Field> fields = getAllFields(testInstance.getClass());

    for (Field field : fields) {
      try {
        // Skip static fields - they're handled in beforeAll
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }

        // Check if field is accessible
        boolean wasAccessible = field.canAccess(testInstance);
        if (!wasAccessible) {
          field.setAccessible(true);
        }

        // Get the field value
        Object fieldValue = field.get(testInstance);

        // Skip if not a container or if it's null
        if (fieldValue == null) {
          continue;
        }

        // Check if it's a GenericContainer or a subclass
        if (GenericContainer.class.isAssignableFrom(field.getType())) {
          applyReuseToContainer((GenericContainer<?>) fieldValue, field.getName());
        }

        // Restore accessibility
        if (!wasAccessible) {
          field.setAccessible(false);
        }
      } catch (Exception e) {
        LOG.warn("Failed to apply reuse to instance field: " + field.getName(), e);
      }
    }
  }

  private List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    Class<?> currentClass = clazz;

    // Walk up the inheritance hierarchy to get all fields
    while (currentClass != null && currentClass != Object.class) {
      fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
      currentClass = currentClass.getSuperclass();
    }

    return fields;
  }

  private <T extends GenericContainer<?>> void applyReuseToContainer(
      T container, String fieldName) {
    try {
      // Get the withReuse method
      Method withReuseMethod = GenericContainer.class.getMethod("withReuse", boolean.class);

      // Call withReuse(true) on the container
      withReuseMethod.invoke(container, true);

      LOG.debug("Successfully applied withReuse(true) to container: {}", fieldName);
    } catch (Exception e) {
      LOG.warn("Failed to apply withReuse to container: " + fieldName, e);
    }
  }
}
