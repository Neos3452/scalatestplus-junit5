package org.scalatestplus.junit5

import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DiscoverySelectors.{selectClass, selectClasspathRoots, selectPackage}
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.scalatest.funspec
import org.scalatestplus.junit5.helpers.HappySuite

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.SetHasAsScala

class ScalaTestEngineSpec extends funspec.AnyFunSpec {
  val engine = new ScalaTestEngine

  describe("ScalaTestEngine") {
    describe("discover method") {
      it("should discover suites on classpath") {
        val classPathRoot = classOf[ScalaTestEngineSpec].getProtectionDomain.getCodeSource.getLocation
        val discoveryRequest = request.selectors(
          selectClasspathRoots(java.util.Set.of(Paths.get(classPathRoot.toURI)))
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.exists(td => td.asInstanceOf[ScalaTestClassDescriptor].suiteClass == classOf[HappySuite]))
      }

      it("should return unresolved for classpath without any tests") {
        val emptyPath = Files.createTempDirectory(null)
        val discoveryRequest = request.selectors(
          selectClasspathRoots(java.util.Set.of(emptyPath))
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.isEmpty)
      }

      it("should discover suites in package") {
        val classPathRoot = classOf[ScalaTestEngineSpec].getProtectionDomain.getCodeSource.getLocation
        val discoveryRequest = request.selectors(
          selectPackage("org.scalatestplus.junit5.helpers")
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.exists(td => td.asInstanceOf[ScalaTestClassDescriptor].suiteClass == classOf[HappySuite]))
      }

      it("should return unresolved for package without any tests") {
        val discoveryRequest = request.selectors(
          selectPackage("org.scalatestplus.junit5.nonexistant")
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.isEmpty)
      }
    }
  }
}
