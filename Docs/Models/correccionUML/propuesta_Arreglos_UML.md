## Dominio

### **Correcciones**

En el modelo de dominio se han incluido envoltorios de respuesta de APIs de terceros (como `NetworksResponse` o `NetworkResponse`), desde la perspectica de dominio puro no deberían de aparecer, pero si se quieren mantener estaría bien añadir una nota aclaratoria:

"Se incluyen las clases wrapper NetworksResponse y NetworkResponse para reflejar la estructura del paquete model implementado para el consumo de la API CityBikes".

En `Location` faltan los atributos country, para poder cumplir con la historia de "Seleccionar preferencias de país y ciudad" y las coordenadas, para poder hacer una ordenación por distancia o por mapa (aunque puede quedarse como opcional, ya que poner coordenadas para el usuario puede ser tedioso y entorpecer el flujo de uso de la aplicación).


## Arquitectura

### **Correcciones**

**La vista accede directamente al repositorio**

`MainView`apunta directamente a `INetworksRepository`, y en la interfaz `View` del contrato está el método `getNetworksRepository() : INetworksRepository`.

En la arquitectura MVP, la Vista nunca debe estar relacionada directamente con la capa de datos/repositorio. La Vista solo debe pintar elemetnos en pantalla y captruar clics. Es el Presentador el único encargado de pedir datos al Repositorio.

*SOLUCION:* Se elimina la asociación entre `MainView` y `INetworksRepository` (así como el método getNetworksRepository de la interfaz `View`) para no violar el principio de separación de responsabilidades del patrón MVP. La Vista no debe tener acceso al Modelo/Repositorio; es el Presenter quien consulta al repositorio.

**Acoplamiento directo entre clases concretas**

La clase `MainView` apunta a la clase `MainPresenter`, y `MainPresenter` apunta a `MainView`, La vista debe depender de la interfaz `IMainContract.Presenter` y el presentador debe depender de la interfaz `IMainContract.View`.

*SOLUCION:* Se desacoplan las clases concretas `MainView` y `MainPresenter`. De este modo `MainView` mantiene una referencia hacia la abstracción `IMainContract.Presenter`, y `MainPresenter` mantiene su referencia hacia `IMainContract.View`.

**El presentador no tiene acceso al repositorio**

`MainPresenter`no tiene ninguna relacion hacia `InetWorksRepository`. Si la vista ya no llama al repositorio, `MainPresenter`debe tener la referencia hacia `INetworksRepository`.

**Interfaces Callback 'Flotantes'**

`NetworksCallback` y `NetworkDetailsCallback` aparecen sueltas en la esquina superior derecha sin ninguna flecha que las vincule. Deben conectarse mediante relaciones de uso (<< use >>) desde `INetworksRepository` (ya que sus métodos reciben estas interfaces como parámetro).

## Diagrama de Secuencia

### **Correcciones**

**Ausencia de parametros en las llamadas**

En el diagrama de arquitectura se han definido qué recibe cada método. Si en este diagrama están vacíos, no concuerdan con los contratos de clases.

**Retorno síncorno de [Networks] en lugar de llamada al Callback**

En Android las llamadas a red son asíncronas y en la arquitectura se ha definido la interfaz NetworksCallback. El repositorio no devuelve un valor mediante una flecha de retorno discontinuo, sino que ejecuta una llamada de método explícita sobre el callback.

**Etiqueta //finished// en retorno de método void**

En la sintaxis formal de UML de secuencia, las flechas discontinuas de retorno solo llevan etiqueta de texto si devuelven un objeto/valor (como [networks]). Si el método es void (como la respuesta a init()), la flecha de retorno debe ir vacía.

## Regenerar los diagramas

Los archivos `.png` se generan a partir de los `.puml` ejecutando el script incluido en este directorio. El script descarga automáticamente PlantUML (`plantuml.jar`) si no está presente, por lo que solo requiere tener Java instalado.

- **macOS / Linux:**

  ```sh
  ./generate_diagrams.sh
  ```

- **Windows:**

  ```bat
  generate_diagrams.bat
  ```