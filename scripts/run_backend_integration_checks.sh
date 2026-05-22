#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_DIR="$ROOT_DIR/AI HEALTH AGENT"
BUILD_DIR="${TMPDIR:-/tmp}/smarthealth-backend-integration-checks"
SERVLET_API="/Applications/Android Studio.app/Contents/plugins/android/lib/javax.servlet-api-3.0.1.jar"
DERBY_JAR="/Users/didintlemakhubedu/NetBeansJDKs/NewFolder/javadb/lib/derby.jar"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

javac -source 8 -target 8 \
  -cp "$APP_DIR/web/WEB-INF/lib/guava-31.1-jre.jar:$APP_DIR/web/WEB-INF/lib/mavenproject2-1.0-SNAPSHOT.jar:$APP_DIR/web/WEB-INF/lib/mariadb-java-client-2.7.2.jar:$APP_DIR/web/WEB-INF/lib/postgresql-42.7.11.jar:$SERVLET_API:$DERBY_JAR" \
  -d "$BUILD_DIR" \
  "$APP_DIR/src/java/za/ac/tut/model/PasswordUtils.java" \
  "$APP_DIR/src/java/za/ac/tut/model/ReportColumn.java" \
  "$APP_DIR/src/java/za/ac/tut/model/ReportCriteria.java" \
  "$APP_DIR/src/java/za/ac/tut/model/ReportResult.java" \
  "$APP_DIR/src/java/za/ac/tut/util/DatabaseConfig.java" \
  "$APP_DIR/src/java/za/ac/tut/util/HealthRiskPredictionService.java" \
  "$APP_DIR/src/java/za/ac/tut/util/JsonUtil.java" \
  "$APP_DIR/src/java/za/ac/tut/util/PasswordResetService.java" \
  "$APP_DIR/src/java/za/ac/tut/util/PatientContextSettingsService.java" \
  "$APP_DIR/src/java/za/ac/tut/util/PatientAccountProcedureService.java" \
  "$APP_DIR/src/java/za/ac/tut/util/ReportService.java" \
  "$APP_DIR/src/java/za/ac/tut/util/VitalAlertEvaluator.java" \
  "$APP_DIR/test/BackendIntegrationChecks.java"

java -DSMARTHEALTH_DB_URL=jdbc:mariadb://localhost/unused \
  -cp "$BUILD_DIR:$SERVLET_API:$DERBY_JAR" BackendIntegrationChecks
