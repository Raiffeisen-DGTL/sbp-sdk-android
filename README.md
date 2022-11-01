# Raiffeisen SBP SDK

СДК предоставляет функционал для перенаправления пользователя для оплаты по СБП.

### Установка

Подключите MavenCentral в build.gradle в корне проекта:

```kotlin
allprojects {
    repositories {
        mavenCentral()
    }
}
```

Подключение в dependencies:

```kotlin
dependencies {
    implementation("ru.raiffeisen:sbp-sdk-android:1.0.0")
}
```

### Простое использование:

```kotlin
SbpRedirectFragment.newInstance(
    link = yourSbpLink
).show(fragmentManager, null)
```

### Получение событий от SbpRedirectFragment

Получение событий от SbpRedirectFragment происходит через FragmentResultListener ([Документация по использованию Fragment Result API](https://developer.android.com/guide/fragments/communicate#fragment-result)).
Для этого необходимо при создании SbpRedirectFragment указать resultKey,
по этому ключу вы сможете прослушивать события:

```kotlin
const val SBP_RESULT_KEY = "your key"

SbpRedirectFragment.newInstance(
    link = yourSbpLink,
    resultKey = SBP_RESULT_KEY
).show(fragmentManager, null)
```

Для прослушивания событий необходимо установить слушатель на fragmentManager,
через который был вызван SbpRedirectFragment.
Для удобного чтения результата события используйте SbpRedirectFragment.readResult(bundle) как в
примере:

```kotlin
fragmentManager.setFragmentResultListener(
    SBP_RESULT_KEY,
    lifecycleOwner
) { _, bundle ->
    when (val result = SbpRedirectFragment.readResult(bundle)) {
        is SbpRedirectFragment.Result.RedirectedToBank -> {
            "redirected to bank ${result.packageName}"
        }
        is SbpRedirectFragment.Result.RedirectedToDownloadBank -> {
            "redirected to download bank ${result.packageName}"
        }
        SbpRedirectFragment.Result.RedirectedToDefaultBank -> {
            "redirected to default bank"
        }
        SbpRedirectFragment.Result.DialogDismissed -> {
            "dialog dismissed"
        }
        is SbpRedirectFragment.Result.RedirectToBankFailed -> {
            "redirect to bank failed ${result.packageName}"
        }
    }
}
```

## Сборка семпл проекта
Просто откройте проект в Android Studio и нажмите «Run».
Так же в корне проекта есть уже собраный файл sample.apk