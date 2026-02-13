import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'eur', standalone: true })
export class EurPipe implements PipeTransform {
  private formatter = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' });

  transform(value: number | null | undefined): string {
    if (value === null || value === undefined) return '-';
    return this.formatter.format(value);
  }
}
