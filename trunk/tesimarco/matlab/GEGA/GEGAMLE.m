%clc;
clear all;
close all;

%if(strcmp(version('-release'),'2009a'))
    if(matlabpool('size') == 0)
        maxNumCompThreads(2);
        matlabpool local 2;
    end;
%end

slots=20;
b=2;
p=1./(b.^(1:60));
Ts=[0:1:132];
%Ts=70
for T=Ts
    tic;
    T
    if exist(['GMLE-T-' int2str(T) '.mat'],'file')==2
        %error(['non calcolato in precedenza per T=' int2str(T)]);
    else
        GMLE=zeros(T+1,T+1,slots);
            % se qualcuno ci stà già lavorando
            if (exist(['lock.GMLE-T-' int2str(T)],'file')~=2)
                %nessuno ci stava  già lavorando
                fid = fopen(['lock.GMLE-T-' int2str(T)],'w+');
                fclose(fid);
                % ci lavoro io
                GMLEwrong=GMLE;
                GMLE=zeros(T+1,T+1,slots);
                if (T~=0)
                    sficsT0='((1-p(l))^n)^i * (n*p(l)*(1-p(l))^(n-1))^s * (1-(1-p(l))^n-n*p(l)*(1-p(l))^(n-1))^c';
                    parfor l=1:slots
                    %for l=1:slots
                        fprintf('%d ',l);
                        sfG0=sfG(l);
                        %sfG1=diff(sfG0,'n');
                        %sficsT1=diff(sficsT0,'n');
                        %sfG1=char(sfG1);
                        %sficsT1=char(sficsT1);
                        sfT0=[sficsT0 ' * '  sfG0];
                        sfT0=strrep(sfT0,'*','.*');
                        sfT0=strrep(sfT0,'^','.^');
                        sfT0=strrep(sfT0,'/','./');
                        fT0=inline(sfT0,'p','n','l','i','s','c');
                        GMLEtmp=GMLEwrong(:,:,l);
                        for c=0:T
                            for s=0:T-c
                                i=T-s-c;
                                %update wrong values
                                sopra=0;
                                if(c>0)
                                    sopra=GMLEtmp(c,s+1);
                                end
                                lato=0;
                                if(s>0)
                                    lato=GMLEtmp(c+1,s);
                                end
                                if (c>0 && GMLEtmp(c+1,s+1)>GMLEtmp(c,s+1))
                                else
                                    GMLEtmp(c+1,s+1)=deasysolve2(fT0,p,l,i,s,c,max([2,sopra,lato]));
                                    %    GMLEtmp(c+1,s+1)=deasysolve(fT0,p,l,i,s,c);
                                end
                                % default computation
                                % GMLEtmp(c+1,s+1)=deasysolve(fT0,p,l,i,s,c);
                                %GMLEtmp(c+1,s+1)=deasysolve2(fT0,p,l,i,s,c,max([2,sopra,lato]));
                            end
                        end
                        GMLE(:,:,l)=GMLEtmp;
                    end
                else
                    parfor l=1:slots
                        GMLE(:,:,l)=2^l;
                    end
                end
                save(['GMLE-T-' int2str(T)], 'GMLE','T','b','slots','p');
                fprintf('\n');
                %segnalo che non li lavoro più
                delete(['lock.GMLE-T-' int2str(T)]);
            else
                disp('qualcuno ci stà già lavorando');
            end
    end
    toc;
end