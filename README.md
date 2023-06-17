# Digital Reminder Library - DRL

Digital Reminder Library (DRL) é uma biblioteca em Java (Android) que tem o objetivo de gerenciar a criação de alarmes, possibilitando que sejam criados baseados ou no horário ou na localização, inclusive,  de um dia da semana específico.

**Foi elaborada durante a disciplina de Programação para dispositivos móveis na graduação em Ciência da Computação**.

## Features

- Gerenciamento de alarmes baseados em um horário específico para um dia da semana;

- Gerenciamento de alarmes baseados em uma localização geográfica especifica para um dia da semana;

- Gerenciamento de alarmes baseados tanto em um horário específico quanto em uma localização especifica para um dia da semana;

- Interface para padronização de operações de geocoding e reverse geocoding;

## Dependencies

- [google play services location framework](https://developers.google.com/android/reference/com/google/android/gms/location/LocationServices);

- Java 8 Standard Library;

- Android API 33 Compile SDK Version;

## Limitations and Notices

- [O máximo de lembretes baseado na localização é 100](https://developer.android.com/training/location/geofencing). Lembretes baseado no tempo e na localização sempre lançam um lembrete de curta vida (~30s), o que pode não acontecer se o limite já tiver sido alcançado;

- Os dados de lembretes baseados na localização são armazenados no armazenamento interno do dispositivo, que são serializados e desserializados frequentemente.

- As versões recente do android, de modo geral, restringem o uso de recursos que são custosos ao dispositivos, o que inclui a [notificaçao de mudanças na localização](https://developer.android.com/about/versions/oreo/background-location-limits), ao qual o DRL é abrangido. A solução é ou fazer a aplicação cliente requisitar frequentemente a localizção do dispositivo ou que alguma aplicação externa o faça, de forma que o DRL possa reaproveitar o resultado obtido.

## How build and use

1. Use 'git clone https://github.com/Manvin1/Digital-Reminder-Library.git' para obter o DRL

2. Insira o DRL como uma dependência de módulo da aplicação alvo.

3. Para registrar alarmes, use Reminder Manager. Para realizar geocoding ou reverse geocoding, use LocationFinder.

4. Caso alarmes baseados em localizações seja usado, é necessário injetar uma instância de GeocodingProvider em LocationFinder.

   - Este objeto será usado para geocoding e reverse geocoding durante o registro de alarmes. DRL não impõe o uso de algum provedor específico, cabendo ao cliente implementar da forma que achar mais conveniente

## Example

- [Digital Reminder](https://github.com/Manvin1/Digital-Reminder)
  
## License

DRL está disponível sob 'dual licensing' e pode ser usado sob as licenças BSD-3 ou CC0.

**Lembre-se que [o Google Play Services Location Framework pode incluir outras bibliotecas open source, ao qual tem especificidades quanto a sua distribuição que devem ser seguidas.](https://developers.google.com/android/guides/opensource#groovy-dsl)**

## Disclaimer

This software library is not affiliated with or endorsed by the authors of the third-party libraries and other resources that were used in its implementation.
